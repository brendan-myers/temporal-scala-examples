import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import io.temporal.activity.ActivityOptions
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.Worker
import io.temporal.worker.WorkerFactory
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.time.Duration

val TASK_QUEUE = "HelloActivityTaskQueue"
val WORKFLOW_ID = "HelloActivityWorkflow"

@WorkflowInterface
trait GreetingWorkflow {
  @WorkflowMethod
  def getGreeting(name: String): String
}

@ActivityInterface
trait GreetingActivities {
  @ActivityMethod(name = "greet")
  def composeGreeting(greeting: String, name: String): String
}

class GreetingWorkflowImpl extends GreetingWorkflow {
  var activities = Workflow.newActivityStub(
    classOf[GreetingActivities],
    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build()
  )

  def getGreeting(name: String): String =
    return activities.composeGreeting("Hello", name)
}

class GreetingActivitiesImpl extends GreetingActivities {
  def composeGreeting(greeting: String, name: String): String =
    return greeting + " " + name
}

@main def hello: Unit =
  val service = WorkflowServiceStubs.newLocalServiceStubs()
  val client = WorkflowClient.newInstance(service)
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

  val greeting = workflow.getGreeting("World")
  println(greeting)

