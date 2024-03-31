package indigo

import kotlin.random.Random

class Player {
    var hand = mutableListOf<String>()
    var score = 0
    var cardsWon = 0
}

class IndigoCardGame {
    private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    private val suits = listOf("♦", "♥", "♠", "♣")
    private var pointSystem = mapOf("A" to 1, "10" to 1, "J" to 1, "Q" to 1, "K" to 1)
    private var deck = mutableListOf<String>()

    private var exit = true
    private var playerTurn = false

    private val table = mutableListOf<String>()
    private val player = Player()
    private val computer= Player()
    private var winner = if (playerTurn) player else computer

    init {
        createDeck()
    }

    fun play() {
        println("Indigo Card Game")
        askPlayerStart()
        initGame()

        while (exit) {
            if (table.isEmpty()) {
                println("\nNo cards on the table")
            } else {
                println("\n${table.size} cards on the table, and the top card is ${table.last()}")
            }
            when {
                playerTurn -> chooseCardPlayer()
                !playerTurn ->chooseCardComputer()
            }
        }

        if (deck.isEmpty()) showScore()
        println("Game Over")
    }

    private fun createDeck() {
        for (i in ranks) {
            for (j in suits) { deck.add(i + j) }
        }
        deck = deck.shuffled().toMutableList()
    }

    private fun askPlayerStart() {
        var playFirst = println("Play first?").run { readln() }
        while ( !Regex("""yes|no""").matches(playFirst.lowercase())){
            playFirst = println("Play first?").run { readln() }
        }
        playerTurn = playFirst.lowercase() == "yes"
    }

    private fun initGame() {
        table.addAll(deck.take(4))
        deck = deck.drop(4).toMutableList()
        drawCards()
        println("Initial cards on the table: ${table.joinToString(" ")}")
    }

    private fun drawCards() {
        player.hand.addAll(deck.take(6))
        deck = deck.drop(6).toMutableList()
        computer.hand.addAll(deck.take(6))
        deck = deck.drop(6).toMutableList()
    }

    private fun chooseCardPlayer() {
        var isCardChosen = true
        print("Cards in hand: ")
        repeat(player.hand.size) {print("${it + 1})${player.hand[it]} ")}
        println()
        var chosenCard = println("Choose a card to play (1-${player.hand.size}):").run { readln() }
        while (isCardChosen) {
            when {
                chosenCard == "exit" -> {
                    exit = false
                    isCardChosen = false
                }
                !Regex("""\d+""").matches(chosenCard) -> chosenCard = println("Choose a card to play (1-${player.hand.size}):").run { readln() }
                chosenCard.toInt() !in 1..player.hand.size -> chosenCard = println("Choose a card to play (1-${player.hand.size}):").run { readln() }
                else -> {
                    isCardChosen = false
                    table.add(player.hand[chosenCard.toInt() - 1])
                    player.hand.removeAt(chosenCard.toInt() - 1)
                }
            }
        }
        playerTurn = false
        if (checkHands() && exit) {
            checkLastCards(player)
        }
    }

    private fun chooseCardComputer() {
        repeat(computer.hand.size) {print("${computer.hand[it]} ")}
        println()

        val cardNumber: Int = when {
            computer.hand.size == 1 -> 0
            table.isEmpty() -> getCard(computer.hand)
            else -> {
                val candidateCards = getCandidateCards()
                when {
                    candidateCards.isEmpty() -> getCard(computer.hand)
                    candidateCards.size == 1 -> candidateCards.first()
                    else -> {
                        candidateCards.first()
                    }
                }
            }
        }

        table.add(computer.hand[cardNumber])
        println("Computer plays ${computer.hand[cardNumber]}")
        computer.hand.removeAt(cardNumber)

        playerTurn = true
        if (checkHands()) checkLastCards(computer)
    }

    private fun getCard(inputMap: MutableList<String>): Int {
        val cardNumber: Int
        val suitMap = getMultipleSuits(inputMap)
        val rankMap = getMultipleRanks(inputMap)

        when {
            suitMap.isNotEmpty() -> {
                val card = suitMap.entries.first().value; card.shuffle()
                cardNumber = card.first()
            }
            rankMap.isNotEmpty() -> {
                val card = rankMap.entries.first().value; card.shuffle()
                cardNumber = card.first()
            }
            else -> {
                cardNumber = Random.nextInt(0, inputMap.size)
            }
        }
        return cardNumber
    }

    private fun getMultipleSuits(inputMap: MutableList<String>): Map<String, MutableList<Int>> {
        val suitMap = mutableMapOf<String, MutableList<Int>>()
        for (i in inputMap.indices) {
            val suit = inputMap[i].takeLast(1)
            if (suitMap.containsKey(suit)) suitMap[suit]!!.add(i) else suitMap[suit] = mutableListOf(i)
        }
        return  suitMap.filter { it.value.size > 1}.toMutableMap()
    }

    private fun getMultipleRanks(inputMap: MutableList<String>): Map<String, MutableList<Int>> {
        var rankMap = mutableMapOf<String, MutableList<Int>>()
        for (i in inputMap.indices) {
            val rank = inputMap[i].dropLast(1)
            if (rankMap.containsKey(rank)) rankMap[rank]!!.add(i) else rankMap[rank] = mutableListOf(i)
        }
        rankMap = rankMap.filter { it.value.size > 1 }.toMutableMap()
        return rankMap
    }

    private fun getCandidateCards(): MutableList<Int> {
        val topCard = table.last()
        val candidateSuitCardMap = mutableListOf<Int>()
        val candidateRankCardMap = mutableListOf<Int>()
        for (i in computer.hand.indices) if (topCard.takeLast(1) == computer.hand[i].takeLast(1)) candidateSuitCardMap.add(i)
        for (i in computer.hand.indices) if (topCard.dropLast(1) == computer.hand[i].dropLast(1)) candidateRankCardMap.add(i)
        return if (candidateSuitCardMap.size < 2 && candidateRankCardMap.isNotEmpty()) candidateRankCardMap else candidateSuitCardMap
    }

    private fun checkHands(): Boolean {
        if (player.hand.isEmpty() && computer.hand.isEmpty() && deck.isEmpty()) {
            endGame()
            return false
        }else if (player.hand.isEmpty() && computer.hand.isEmpty()) {
            drawCards()
            return true
        } else {
            return true
        }
    }

    private fun checkLastCards(person: Player) {
        if (table.size == 1) return
        if (table[table.size - 2].first() == table[table.size - 1].first()
            || table[table.size - 2].last() == table[table.size - 1].last()) {
            var  points = 0
            table.forEach { points += pointSystem[it.dropLast(1)] ?: 0 }
            if (person == player) {
                player.cardsWon += table.size
                player.score += points
                winner = player
                println("Player wins cards")
                showScore()
            } else {
                computer.cardsWon += table.size
                computer.score += points
                winner = computer
                println("Computer wins cards")
                showScore()
            }
            table.clear()
        }
    }

    private fun endGame() {
        exit = false
        if (table.isNotEmpty()) {
            println("\n${table.size} cards on the table, and the top card is ${table.last()}")
            var  points = 0
            table.forEach { points += pointSystem[it.dropLast(1)] ?: 0 }
            if (winner == player) {
                player.cardsWon += table.size
                player.score += points
            } else {
                computer.cardsWon += table.size
                computer.score += points
            }
            table.clear()
        }
        when (player.cardsWon) {
            in 27..52 -> player.score += 3
            26 -> if (winner != player) player.score += 3 else computer.score += 3
            else -> computer.score += 3
        }
    }

    private fun showScore() {
        println("Score: Player ${player.score} - Computer ${computer.score}")
        println("Cards: Player ${player.cardsWon} - Computer ${computer.cardsWon}")

    }
}

fun main() {
    val game = IndigoCardGame()
    game.play()
}