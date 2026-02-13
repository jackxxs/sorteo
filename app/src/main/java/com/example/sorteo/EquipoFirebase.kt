package com.example.sorteo

data class EquipoFirebase(
    val miembros: List<String> = emptyList(),
    val ganador: Boolean = false,
    val fase: String = "Sorteo",
    val reclamandoVictoria: Boolean = false,
    val haDescansado: Boolean = false
)
