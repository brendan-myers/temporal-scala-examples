import io.temporal.workflow.{QueryMethod, SignalMethod, WorkflowInterface, WorkflowMethod}
import model.{GreetingSignalInput, GreetingWorkflowInput, GreetingWorkflowOutput}

@WorkflowInterface
trait GreetingWorkflow {
  @WorkflowMethod
  def getGreeting(request: GreetingWorkflowInput): GreetingWorkflowOutput

  @SignalMethod
  def changeGreeting(request: GreetingSignalInput): Unit

  @QueryMethod
  def checkGreeting(param: String): String
}
