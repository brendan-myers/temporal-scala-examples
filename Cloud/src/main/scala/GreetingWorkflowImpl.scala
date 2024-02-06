import io.temporal.activity.ActivityOptions
import io.temporal.common.converter.EncodedValues
import io.temporal.workflow.{DynamicSignalHandler, Workflow}
import model.{GreetingActivityInput, GreetingSignalInput, GreetingWorkflowInput, GreetingWorkflowOutput}

import java.time.Duration

class GreetingWorkflowImpl extends GreetingWorkflow {
  private val logger = Workflow.getLogger(classOf[GreetingWorkflowImpl])

  var greeting = "Hello"

  // Register a dynamic signal handler
  Workflow.registerListener((
    (signalName: String, encodedArgs: EncodedValues) =>
      logger.info("Dynamic Signal Handler: " + signalName + ", " + encodedArgs.get(classOf[String]))
    ): DynamicSignalHandler
  )

  // Signal handler
  override def changeGreeting(request: GreetingSignalInput): Unit =
    logger.info("Changed greeting: " + request.greeting)
    greeting = request.greeting

  // Query handler
  override def checkGreeting(param: String): String =
    logger.info("Checking greeting: " + greeting)
    greeting

  private val activities: GreetingActivities = Workflow.newActivityStub(
    classOf[GreetingActivities],
    ActivityOptions.newBuilder()
      .setStartToCloseTimeout(Duration.ofSeconds(2))
      .build()
  )

  override def getGreeting(request: GreetingWorkflowInput): GreetingWorkflowOutput = {
    logger.info("Starting workflow")
    Workflow.sleep(Duration.ofSeconds(30))

    val activityInput = GreetingActivityInput(greeting, request.name)
    val activityOutput = activities.composeGreeting(activityInput)
    GreetingWorkflowOutput(activityOutput.personalGreeting)
  }
}
