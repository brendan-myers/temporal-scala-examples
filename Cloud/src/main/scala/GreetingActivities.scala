import io.temporal.activity.{ActivityInterface, ActivityMethod}
import model.{GreetingActivityInput, GreetingActivityOutput}

@ActivityInterface
trait GreetingActivities {
  @ActivityMethod(name = "greet")
  def composeGreeting(request: GreetingActivityInput): GreetingActivityOutput
}
