import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.temporal.client.{WorkflowClient, WorkflowClientOptions, WorkflowOptions}
import io.temporal.common.converter.{DefaultDataConverter, JacksonJsonPayloadConverter}
import io.temporal.serviceclient.{SimpleSslContextBuilder, WorkflowServiceStubs, WorkflowServiceStubsOptions}
import io.temporal.worker.WorkerFactory
import model.GreetingWorkflowInput

import java.io.FileInputStream

object Main {
  private val TASK_QUEUE = "GreetingTaskQueue"
  private val WORKFLOW_ID = "GreetingWorkflow"

  def main(args: Array[String]): Unit = {
    val clientCertFile = System.getenv("TEMPORAL_CLOUD_CERT")
    val clientKeyFile = System.getenv("TEMPORAL_CLOUD_KEY")

    val clientCertInputStream = new FileInputStream(clientCertFile)
    val clientKeyInputStream = new FileInputStream(clientKeyFile)

    val sslContext = SimpleSslContextBuilder.forPKCS8(
      clientCertInputStream,
      clientKeyInputStream
    ).build()

    val namespace = System.getenv("TEMPORAL_CLOUD_NAMESPACE")
    val port = System.getenv("TEMPORAL_CLOUD_PORT")
    val hostPort = namespace + ".tmprl.cloud:" + port

    val stubsOptions: WorkflowServiceStubsOptions =
      WorkflowServiceStubsOptions.newBuilder()
        .setSslContext(sslContext)
        .setTarget(hostPort)
        .build()

    val service = WorkflowServiceStubs.newServiceStubs(stubsOptions)

    // The default JacksonJsonPayload used in DefaultDataConverter
    // can't serialize Scala objects, so we override it.
    //
    val mapper = ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val clientOptions = WorkflowClientOptions.newBuilder()
      .setDataConverter(
        DefaultDataConverter.newDefaultInstance()
          .withPayloadConverterOverrides(
            new JacksonJsonPayloadConverter(mapper)
          ))
      .setNamespace(namespace)
      .build()

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
    println(greeting)
  }
}
