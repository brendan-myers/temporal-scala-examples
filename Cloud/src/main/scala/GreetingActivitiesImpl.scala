import model.{GreetingActivityInput, GreetingActivityOutput}

class GreetingActivitiesImpl extends GreetingActivities {
  override def composeGreeting(request: GreetingActivityInput): GreetingActivityOutput =
    GreetingActivityOutput(request.greeting + ", " + request.name)
}
