import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import io.temporal.activity.ActivityOptions
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.client.WorkflowOptions
import io.temporal.common.converter.DefaultDataConverter
import io.temporal.common.converter.JacksonJsonPayloadConverter
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.Worker
import io.temporal.worker.WorkerFactory
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.time.Duration

val TASK_QUEUE = "HelloActivityTaskQueue"
val WORKFLOW_ID = "HelloActivityWorkflow"

case class GreetingWorkflowInput(var name: String)
case class GreetingActivityInput(var greeting: String, var name: String)
case class GreetingOutput(var personalGreeting: String)

@WorkflowInterface
trait GreetingWorkflow {
  @WorkflowMethod
  def getGreeting(input: GreetingWorkflowInput): GreetingOutput
}

@ActivityInterface
trait GreetingActivities {
  @ActivityMethod(name = "greet")
  def composeGreeting(request: GreetingActivityInput): GreetingOutput
}

class GreetingWorkflowImpl extends GreetingWorkflow {
  var activities = Workflow.newActivityStub(
    classOf[GreetingActivities],
    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build()
  )

  def getGreeting(request: GreetingWorkflowInput): GreetingOutput =
    return activities.composeGreeting(GreetingActivityInput("Hello", request.name))
}

class GreetingActivitiesImpl extends GreetingActivities {
  def composeGreeting(request: GreetingActivityInput): GreetingOutput =
    return GreetingOutput(request.greeting + " " + request.name)
}

@main def hello: Unit =
  val service = WorkflowServiceStubs.newLocalServiceStubs()

  val mapper = ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val clientOptions = WorkflowClientOptions.newBuilder()
    .setDataConverter(
      DefaultDataConverter.newDefaultInstance()
        .withPayloadConverterOverrides(
          new JacksonJsonPayloadConverter(mapper)
        )
    ).build()
  val client = WorkflowClient.newInstance(service, clientOptions)
  val factory = WorkerFactory.newInstance(client)

  val worker = factory.newWorker(TASK_QUEUE)
  worker.registerWorkflowImplementationTypes(classOf[GreetingWorkflowImpl])
  worker.registerActivitiesImplementations(new GreetingActivitiesImpl())
  factory.start()

  val workflow: GreetingWorkflow = client.newWorkflowStub(
    classOf[GreetingWorkflow],
    WorkflowOptions.newBuilder()
      .setWorkflowId(WORKFLOW_ID)
      .setTaskQueue(TASK_QUEUE)
      .build()
  )

  val greeting = workflow.getGreeting(GreetingWorkflowInput("World"))
  println(greeting.personalGreeting)

