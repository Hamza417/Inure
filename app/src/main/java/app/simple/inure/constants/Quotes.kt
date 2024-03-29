package app.simple.inure.constants

object Quotes {
    val quotes = arrayOf(
        quote("Simplicity is the ultimate sophistication.") + author("Leonardo da Vinci"),
        quote("Life becomes easier when you learn to accept the apology you never got.") + author("R. Brault"),
        quote("Normality is a paved road: it’s comfortable to walk but no flowers grow.") + author("Vincent van Gogh"),
        quote("If you don’t make mistakes, you’re not working on hard enough problems. And that’s a big mistake.") + author("Frank Wilczek"),
        quote("There is a great difference between worry and concern. A worried person sees a problem, and a concerned person solves a problem.") + author("Harold Stephens"),
        quote("“There are times when we stop, we sit still. We listen and breezes from a whole other world begin to whisper.") + author("James Carroll"),
        quote("I don’t know the key to success, but the key to failure is trying to please everybody.") + author("Bill Cosby"),
        quote("The greatest glory in living lies not in never falling, but in rising every time we fall.") + author("Nelson Mandela"),
        quote("Your time is limited, so don't waste it living someone else's life. Don't be trapped by dogma – which is living with the results of other people's thinking.") + author("Steve Jobs"),
        quote("If you look at what you have in life, you'll always have more. If you look at what you don't have in life, you'll never have enough.") + author("Oprah Winfrey"),
        quote("If you set your goals ridiculously high and it's a failure, you will fail above everyone else's success.") + author("James Cameron"),
        quote("Life is what happens when you're busy making other plans.") + author("John Lennon"),
        quote("Within you, there is a stillness and a sanctuary to which you can retreat at any time and be yourself.") + author("Hermann Hesse"),
        quote("Spread love everywhere you go. Let no one ever come to you without leaving happier.") + author("Mother Teresa"),
        quote("You will face many defeats in life, but never let yourself be defeated.") + author("Maya Angelo"),
        quote("Never let the fear of striking out keep you from playing the game.") + author("Babe Ruth"),
        quote("Many of life's failures are people who did not realize how close they were to success when they gave up.") + author("Thomas A. Edison"),
        quote("The only impossible journey is the one you never begin.") + author("Tony Robbins"),
        quote("Go confidently in the direction of your dreams! Live the life you've imagined.") + author("Henry David Thoreau"),
        quote("In three words I can sum up everything I've learned about life: it goes on.") + author("Robert Frost"),
        quote("If you really look closely, most overnight successes took a long time.") + author("Steve Jobs"),
        quote("Set peace of mind as your highest goal, and organize your life around it.") + author("Brian Tracy"),
        quote("Forget all the reasons why it won't work, and believe the one reason why it will.") + author("Ziad K. Abdelnour"),
        quote("When you realize how perfect everything is you will tilt your head back and laugh at the sky.") + author("Buddha"),
        quote("This moment will just be another story someday.") + author("Stephen Chbosky"),
        quote("You are the sky. Everything else is just the weather.") + author("Pema Chodron"),
        quote("You cannot perceive beauty, but with a serene mind.") + author("Henry David Thoreau")
    )

    private fun author(string: String): String {
        return "<br/> <small><h6><font color=%%%>- $string</font></h6></small>"
    }

    private fun quote(string: String): String {
        return "<h1>$string</h1>"
    }
}
