package com.example.sorteo

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseManager {
    private val database = FirebaseDatabase.getInstance().reference.child("torneo_petanca")

    fun publicarSorteo(equipos: List<EquipoFirebase>) {
        database.setValue(equipos)
    }

    fun reclamarVictoria(miIndex: Int) {
        database.child(miIndex.toString()).child("reclamandoVictoria").setValue(true)
    }

    fun confirmarResultado(ganadorIndex: Int, perdedorIndex: Int?, faseActual: String) {
        database.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val equipos = currentData.children.mapNotNull { it.getValue(EquipoFirebase::class.java) }
                if (equipos.isEmpty()) return Transaction.success(currentData)

                val newEquipos = equipos.toMutableList()

                // Asegurarse de que los índices son válidos
                val ganador = newEquipos.getOrNull(ganadorIndex) ?: return Transaction.abort()
                var ganadorActualizado = ganador.copy(reclamandoVictoria = false)

                // Gestionar al perdedor
                if (perdedorIndex != null) {
                    val perdedor = newEquipos.getOrNull(perdedorIndex) ?: return Transaction.abort()
                    val nuevaFasePerdedor = when (faseActual) {
                        "General" -> "Consolación"
                        "Consolación" -> "Repesca"
                        else -> "Repesca" // El perdedor de Repesca queda eliminado
                    }
                    newEquipos[perdedorIndex] = perdedor.copy(fase = nuevaFasePerdedor, reclamandoVictoria = false)
                }

                // Nueva Lógica de Emparejamiento para la fase General
                if (faseActual == "General") {
                    if (perdedorIndex != null) { // Es una partida real, no un descanso
                        // Buscar si hay un equipo descansando en la fase General
                        val restingTeamIndex = newEquipos.indexOfFirst { it.fase == "General" && it.haDescansado }

                        if (restingTeamIndex != -1) {
                            // Si hay alguien descansando, se emparejan para la siguiente ronda
                            val restingTeam = newEquipos[restingTeamIndex]
                            newEquipos[restingTeamIndex] = restingTeam.copy(haDescansado = false)
                            ganadorActualizado = ganadorActualizado.copy(haDescansado = false) // El ganador no descansa, juega
                        } else {
                            // Si no hay nadie descansando, el ganador pasa a descansar
                            ganadorActualizado = ganadorActualizado.copy(haDescansado = true)
                        }
                    } else { // Es un descanso por ser impar
                        ganadorActualizado = ganadorActualizado.copy(haDescansado = true)
                    }
                }

                newEquipos[ganadorIndex] = ganadorActualizado

                currentData.value = newEquipos
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                // La transacción ha finalizado. Se pueden registrar errores si es necesario.
            }
        })
    }

    fun proclamarCampeon(index: Int) {
        val updates = hashMapOf<String, Any>(
            "$index/ganador" to true,
            "$index/reclamandoVictoria" to false
        )
        database.updateChildren(updates)
    }

    fun rechazarVictoria(ganadorIndex: Int) {
        database.child(ganadorIndex.toString()).child("reclamandoVictoria").setValue(false)
    }

    fun escucharSorteo(): Flow<List<EquipoFirebase>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(EquipoFirebase::class.java) }
                trySend(lista)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }
}