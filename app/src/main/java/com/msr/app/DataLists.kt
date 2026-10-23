package com.msr.app

import kotlin.random.Random

object DataLists {
    val formParams = listOf(
        "QBLH", "QBRE", "HDRSC1", "LGWQS1", "LGWQS2", "LGWQS3",
        "R5FD", "R5FD1", "R5FD2", "R5FD3", "R5FD4", "R5FD5", "R5FD6", "R5FD7",
        "QSRE1", "QSRE2", "QSRE3", "QSRE4", "QSRE5", "QSRE6", "QSRE7", "QSRE8"
    )

    val games = listOf(
        "Ancestors: The Humankind Odyssey", "Angry Birds 2", "Animal Crossing",
        "Anno 1800", "Apex Legends", "Arma 3", "Asphalt 9: Legends", "Assassin's Creed Valhalla",
        "Batman: Arkham Knight", "Battlefield V", "BioShock Infinite", "Call of Duty: Warzone",
        "Counter-Strike 2", "Cuphead", "Diablo IV", "Elden Ring", "Fallout 4", "Far Cry 6",
        "Fortnite", "Forza Horizon 5", "God of War", "Grand Theft Auto V", "Halo Infinite",
        "Hitman 3", "Horizon Forbidden West", "Minecraft", "Mortal Kombat 11", "Need for Speed Heat",
        "PUBG: BATTLEGROUNDS", "Pokemon Scarlet", "Red Dead Redemption 2", "Resident Evil Village",
        "Sea of Thieves", "Silent Hill 2", "The Elder Scrolls V: Skyrim", "The Last of Us",
        "The Legend of Zelda: Tears of the Kingdom", "The Witcher 3", "Terraria", "Valorant"
    )

    val cars = listOf(
        "Alfa Romeo Giulia", "Audi R8", "BMW M3", "Chevrolet Camaro", "Dodge Challenger",
        "Ferrari SF90", "Ford Mustang", "Honda Civic Type R", "Hyundai N Vision 74",
        "Jaguar F-Type", "Lamborghini Huracan", "Lexus LFA", "Mazda MX-5 Miata",
        "McLaren 720S", "Mercedes-AMG GT", "Nissan GT-R", "Porsche 911 GT3 RS",
        "Subaru WRX STI", "Toyota Supra", "Volkswagen Golf R"
    )

    val songs = listOf(
        "(I Can't Get No) Satisfaction", "A Day in the Life", "All Along the Watchtower",
        "Back in Black", "Billie Jean", "Bohemian Rhapsody", "Born to Run", "Comfortably Numb",
        "Hotel California", "Imagine", "Johnny B. Goode", "Like a Rolling Stone", "Purple Haze",
        "Respect", "Smells Like Teen Spirit", "Stairway to Heaven", "Sweet Child O' Mine",
        "What's Going On", "Yesterday"
    )

    val artists = listOf(
        "AC/DC", "Aerosmith", "Aretha Franklin", "Beastie Boys", "Beatles", "Bob Dylan",
        "Bruce Springsteen", "David Bowie", "Eminem", "Fleetwood Mac", "Guns N' Roses",
        "Jimi Hendrix", "Led Zeppelin", "Michael Jackson", "Nirvana", "Pink Floyd",
        "Queen", "Radiohead", "The Rolling Stones", "U2"
    )

    val characters = listOf(
        "Agent 47", "Arthur Morgan", "Cloud Strife", "Commander Shepard", "Doom Guy",
        "Ezio Auditore", "Geralt of Rivia", "Gordon Freeman", "Joel Miller", "Kratos",
        "Lara Croft", "Link", "Mario", "Master Chief", "Nathan Drake", "Pac-Man",
        "Pikachu", "Ryu", "Sephiroth", "Sonic the Hedgehog"
    )

    val movies = listOf(
        "12 Angry Men", "2001: A Space Odyssey", "Alien", "Apocalypse Now", "Casablanca",
        "Citizen Kane", "Fight Club", "Forrest Gump", "Gladiator", "Goodfellas",
        "Inception", "Interstellar", "Parasite", "Pulp Fiction", "Schindler's List",
        "Se7en", "The Dark Knight", "The Godfather", "The Matrix", "The Shawshank Redemption"
    )

    private val allLists = listOf(games, cars, songs, artists, characters, movies)

    fun getRandomTerm(): String {
        val selectedList = allLists[Random.nextInt(allLists.size)]
        return selectedList[Random.nextInt(selectedList.size)]
    }

    fun getRandomForm(): String {
        return formParams[Random.nextInt(formParams.size)]
    }
}
