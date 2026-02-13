package com.example.sorteo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.sorteo.ui.theme.SORTEOTheme
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Funciones de Ayuda para Compartir ---

fun formatSorteoRapidoForShare(resultado: List<List<String>>): String {
    return buildString {
        append("--- RESULTADO DEL SORTEO RÁPIDO ---\n\n")
        resultado.forEachIndexed { index, equipo ->
            append("EQUIPO ${index + 1}:\n")
            equipo.forEach { nombre ->
                append(" • $nombre\n")
            }
            append("\n")
        }
    }
}

fun formatTorneoForShare(equipos: List<EquipoFirebase>, codigo: String, fases: List<String>): String {
    return buildString {
        append("--- TORNEO: $codigo ---\n\n")
        if (equipos.isEmpty()) {
            append("Aún no hay datos del torneo.")
            return@buildString
        }
        fases.forEach { fase ->
            val equiposEnFase = equipos.filter { it.fase == fase }
            if (equiposEnFase.isNotEmpty()) {
                append("--- FASE: ${fase.uppercase()} ---\n")
                val partidas = equiposEnFase.chunked(2)
                partidas.forEach { pareja ->
                    val eqA = pareja[0].miembros.joinToString(" y ")
                    if (pareja.size > 1) {
                        val eqB = pareja[1].miembros.joinToString(" y ")
                        append("$eqA vs $eqB\n")
                    } else {
                        append("$eqA (Pasa a siguiente ronda)\n")
                    }
                }
                append("\n")
            }
        }
    }
}

// --- Fin de Funciones de Ayuda ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            SORTEOTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PantallaPrincipal()
                }
            }
        }
    }
}

enum class UserRole {
    NONE,
    PLAYER,
    CREATOR,
    QUICK_DRAW
}

@Composable
fun PantallaPrincipal() {
    var userRole by remember { mutableStateOf(UserRole.NONE) }

    when (userRole) {
        UserRole.NONE -> {
            PantallaSeleccionRol { role -> userRole = role }
        }
        UserRole.PLAYER -> {
            VistaJugadorConCodigo { userRole = UserRole.NONE }
        }
        UserRole.CREATOR -> {
            VistaCreadorConCodigo { userRole = UserRole.NONE }
        }
        UserRole.QUICK_DRAW -> {
            VistaSorteoRapido { userRole = UserRole.NONE }
        }
    }
}

@Composable
fun PantallaSeleccionRol(onRoleSelected: (UserRole) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PETANCA LIVE ⚾", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color.Red)
        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = { onRoleSelected(UserRole.QUICK_DRAW) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(50.dp)
        ) {
            Text("Sorteo Rápido", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { onRoleSelected(UserRole.CREATOR) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(50.dp)
        ) {
            Text("Creador del torneo", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { onRoleSelected(UserRole.PLAYER) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(50.dp)
        ) {
            Text("Jugador", fontSize = 16.sp)
        }
    }
}

@Composable
fun VistaSorteoRapido(onBack: () -> Unit) {
    var nombres by remember { mutableStateOf(listOf<String>()) }
    var nombreTexto by remember { mutableStateOf("") }
    var sorteoResult by remember { mutableStateOf<List<List<String>>?>(null) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (sorteoResult != null) sorteoResult = null else onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = if (sorteoResult == null) "Sorteo Rápido" else "Resultado del Sorteo",
                fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black
            )
            if (sorteoResult != null) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    val textoCompartir = formatSorteoRapidoForShare(sorteoResult!!)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, textoCompartir)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Compartir Resultado")
                    context.startActivity(shareIntent)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir")
                }
            }
        }

        if (sorteoResult != null) {
            PantallaResultadoSorteo(resultado = sorteoResult!!)
        } else {
            SelectorSorteoRapido(
                nombres = nombres,
                onNombresChange = { nombres = it },
                onTextoChange = { nombreTexto = it },
                texto = nombreTexto,
                onSorteo = { modo ->
                    val tamano = if (modo == "dupletas") 2 else 3
                    sorteoResult = nombres.shuffled().chunked(tamano)
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SelectorSorteoRapido(
    nombres: List<String>,
    onNombresChange: (List<String>) -> Unit,
    onTextoChange: (String) -> Unit,
    texto: String,
    onSorteo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier.fillMaxSize(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
        Column(Modifier.padding(16.dp)) {
            Text("INSCRIPCIÓN DE JUGADORES", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.Black)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                label = { Text("Nombre del Jugador") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedLabelColor = Color.DarkGray,
                    unfocusedLabelColor = Color.DarkGray
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        if (texto.isNotBlank()) {
                            onNombresChange(nombres + texto.trim())
                            onTextoChange("")
                        }
                    }) { Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary) }
                }
            )

            Text("Inscritos: ${nombres.size}", Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, color = Color.DarkGray)

            Box(
                Modifier.weight(1f).fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                if (nombres.isEmpty()) {
                    Text("No hay jugadores añadidos", Modifier.align(Alignment.Center), color = Color.Gray)
                } else {
                    LazyColumn(Modifier.padding(4.dp)) {
                        itemsIndexed(nombres) { index, nombre ->
                            Row(
                                Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}. $nombre", fontWeight = FontWeight.Medium, color = Color.Black)
                                IconButton(onClick = {
                                    val nuevaLista = nombres.toMutableList()
                                    nuevaLista.removeAt(index)
                                    onNombresChange(nuevaLista)
                                }) {
                                    Icon(Icons.Default.Delete, "Borrar", tint = Color(0xFFD32F2F))
                                }
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSorteo("dupletas") }, Modifier.weight(1f), enabled = nombres.size >= 2) { Text("SORTEO DUO") }
                Button(onClick = { onSorteo("tripletas") }, Modifier.weight(1f), enabled = nombres.size >= 3) { Text("SORTEO TRIO") }
            }
        }
    }
}

@Composable
fun PantallaResultadoSorteo(resultado: List<List<String>>) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        itemsIndexed(resultado) { index, equipo ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Equipo ${index + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    equipo.forEach { nombre ->
                        Text(" - $nombre", fontSize = 16.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}


// --- VISTA JUGADOR (REFACTORIZADA) ---

@Composable
fun VistaJugadorConCodigo(onBack: () -> Unit) {
    var codigoTorneo by remember { mutableStateOf("") }
    var torneoIniciado by remember { mutableStateOf(false) }

    if (!torneoIniciado) {
        PantallaIntroducirCodigo(
            codigo = codigoTorneo,
            onCodigoChange = { if (it.length <= 5) codigoTorneo = it.uppercase() },
            onUnirse = {
                if (codigoTorneo.isNotBlank()) {
                    torneoIniciado = true
                }
            },
            onBack = onBack
        )
    } else {
        VistaJugador(
            codigoTorneo = codigoTorneo,
            onBack = {
                torneoIniciado = false
                codigoTorneo = ""
            }
        )
    }
}

@Composable
fun PantallaIntroducirCodigo(
    codigo: String,
    onCodigoChange: (String) -> Unit,
    onUnirse: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("UNIRSE A TORNEO", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = codigo,
            onValueChange = onCodigoChange,
            label = { Text("Código del Torneo") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onUnirse,
            enabled = codigo.length == 5,
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
        ) {
            Text("UNIRSE")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun VistaJugador(codigoTorneo: String, onBack: () -> Unit) {
    val firebaseManager = remember(codigoTorneo) { FirebaseManager(codigoTorneo) }
    val equiposInternet by firebaseManager.escucharSorteo().collectAsState(initial = emptyList())
    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val fases = listOf("General", "Consolación", "Repesca")
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionContainer {
                Text("TORNEO: $codigoTorneo ⚾", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
            }
            Row {
                IconButton(onClick = {
                    val textoCompartir = formatTorneoForShare(equiposInternet, codigoTorneo, fases)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, textoCompartir)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Compartir Torneo")
                    context.startActivity(shareIntent)
                }) {
                    Icon(Icons.Default.Share, "Compartir")
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
                }
            }
        }

        if (equiposInternet.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Esperando datos del torneo...", fontSize = 16.sp, color = Color.DarkGray)
                }
            }
        } else {
            ScrollableTabRow(selectedTabIndex = tabSeleccionada, edgePadding = 0.dp, containerColor = Color.Transparent) {
                fases.forEachIndexed { index, t ->
                    Tab(selected = tabSeleccionada == index, onClick = { tabSeleccionada = index }) {
                        Text(t, modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).padding(top = 12.dp)) {
                val faseActual = fases[tabSeleccionada]
                val filtrados = equiposInternet.filter { it.fase == faseActual }
                val partidas = filtrados.chunked(2)

                itemsIndexed(partidas) { _, partida ->
                    PartidaItem(
                        partida = partida,
                        equipos = equiposInternet,
                        faseActual = faseActual,
                        firebaseManager = firebaseManager
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PartidaItem(
    partida: List<EquipoFirebase>,
    equipos: List<EquipoFirebase>,
    faseActual: String,
    firebaseManager: FirebaseManager
) {
    val eqA = partida.getOrNull(0)
    val eqB = partida.getOrNull(1)
    val indexA = if (eqA != null) equipos.indexOf(eqA) else -1
    val indexB = if (eqB != null) equipos.indexOf(eqB) else -1

    val esGanador = eqA?.ganador == true || eqB?.ganador == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = if(esGanador) Color(0xFFD4EDDA) else Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (eqA != null) {
                EquipoRow(
                    equipo = eqA,
                    rival = eqB,
                    esGanadorTorneo = eqA.ganador,
                    onReclamar = { firebaseManager.reclamarVictoria(indexA) },
                    onConfirmarDerrota = { firebaseManager.confirmarResultado(indexB, indexA, faseActual) },
                    onNegarVictoria = { firebaseManager.rechazarVictoria(indexB) },
                    onPasarDescanso = { firebaseManager.confirmarResultado(indexA, null, faseActual) }
                )
            }

            if (eqB != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
                EquipoRow(
                    equipo = eqB,
                    rival = eqA,
                    esGanadorTorneo = eqB.ganador,
                    onReclamar = { firebaseManager.reclamarVictoria(indexB) },
                    onConfirmarDerrota = { firebaseManager.confirmarResultado(indexA, indexB, faseActual) },
                    onNegarVictoria = { firebaseManager.rechazarVictoria(indexA) },
                    onPasarDescanso = { /* No aplicable */ }
                )
            }
        }
    }
}

@Composable
fun EquipoRow(
    equipo: EquipoFirebase,
    rival: EquipoFirebase?,
    esGanadorTorneo: Boolean,
    onReclamar: () -> Unit,
    onConfirmarDerrota: () -> Unit,
    onNegarVictoria: () -> Unit,
    onPasarDescanso: () -> Unit
) {
    val estaReclamando = equipo.reclamandoVictoria
    val rivalReclama = rival?.reclamandoVictoria ?: false

    val rowColor = if (estaReclamando) Color(0xFFFFF3CD) else Color.Transparent

    Row(
        modifier = Modifier.fillMaxWidth().background(rowColor).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(equipo.miembros.joinToString(", "), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            if (esGanadorTorneo) {
                Text("¡CAMPEÓN! 🏆", color = Color(0xFF155724), fontWeight = FontWeight.Bold)
            } else if (equipo.haDescansado) {
                Text("Pasa de ronda", color = Color.DarkGray, fontSize = 12.sp)
            }
        }

        if (esGanadorTorneo) {
            // Sin botones para el campeón
        } else if (rivalReclama) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onConfirmarDerrota, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))) { Text("V") }
                Button(onClick = onNegarVictoria, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545))) { Text("X") }
            }
        } else if (estaReclamando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else if (rival == null) {
            Button(onClick = onPasarDescanso) { Text("Pasar") }
        } else {
            Button(onClick = onReclamar) { Text("Ganada") }
        }
    }
}


// --- VISTA CREADOR (REFACTORIZADA) ---

fun generarCodigoTorneo(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..5)
        .map { Random.nextInt(0, chars.length) }
        .map(chars::get)
        .joinToString("")
}

@Composable
fun VistaCreadorConCodigo(onBack: () -> Unit) {
    var codigoTorneo by remember { mutableStateOf<String?>(null) }
    var isAdmin by remember { mutableStateOf(false) }
    var claveInput by remember { mutableStateOf("") }

    if (!isAdmin) {
        PantallaLoginAdmin(
            clave = claveInput,
            onClaveChange = { claveInput = it },
            onLogin = { if (claveInput == "1234") isAdmin = true },
            onBack = onBack
        )
    } else if (codigoTorneo == null) {
        PantallaSetupCreador(
            onPublicar = { equipos ->
                val nuevoCodigo = generarCodigoTorneo()
                val firebaseManager = FirebaseManager(nuevoCodigo)
                firebaseManager.publicarSorteo(equipos)
                codigoTorneo = nuevoCodigo
            },
            onBack = { isAdmin = false }
        )
    } else {
        VistaJugador(
            codigoTorneo = codigoTorneo!!,
            onBack = { codigoTorneo = null }
        )
    }
}

@Composable
fun PantallaLoginAdmin(
    clave: String,
    onClaveChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("ACCESO CREADOR", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = clave,
            onValueChange = onClaveChange,
            label = { Text("Clave de Administrador") },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
        ) {
            Text("ENTRAR")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun PantallaSetupCreador(
    onPublicar: (List<EquipoFirebase>) -> Unit,
    onBack: () -> Unit
) {
    var nombres by remember { mutableStateOf(listOf<String>()) }
    var nombreTexto by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text("Configurar Torneo", fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp), color = Color.Black)
        }

        Card(Modifier.fillMaxSize().padding(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
            Column(Modifier.padding(16.dp)) {
                Text("INSCRIPCIÓN DE JUGADORES", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.Black)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = nombreTexto,
                    onValueChange = { nombreTexto = it },
                    label = { Text("Nombre del Jugador") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color.DarkGray,
                        unfocusedLabelColor = Color.DarkGray
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (nombreTexto.isNotBlank()) {
                                nombres = nombres + nombreTexto.trim()
                                nombreTexto = ""
                            }
                        }) { Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                )

                Text("Inscritos: ${nombres.size}", Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, color = Color.DarkGray)

                Box(
                    Modifier.weight(1f).fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    if (nombres.isEmpty()) {
                        Text("No hay jugadores añadidos", Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(Modifier.padding(4.dp)) {
                            itemsIndexed(nombres) { index, nombre ->
                                Row(
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}. $nombre", fontWeight = FontWeight.Medium, color = Color.Black)
                                    IconButton(onClick = {
                                        val nuevaLista = nombres.toMutableList()
                                        nuevaLista.removeAt(index)
                                        nombres = nuevaLista
                                    }) {
                                        Icon(Icons.Default.Delete, "Borrar", tint = Color(0xFFD32F2F))
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val equiposMezclados = nombres.shuffled().chunked(2)
                            val equiposFirebase = equiposMezclados.map {
                                EquipoFirebase(miembros = it, fase = "General")
                            }
                            onPublicar(equiposFirebase)
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = nombres.size >= 2
                    ) {
                        Text("PUBLICAR DUPLAS", fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                    Button(
                        onClick = {
                            val equiposMezclados = nombres.shuffled().chunked(3)
                            val equiposFirebase = equiposMezclados.map {
                                EquipoFirebase(miembros = it, fase = "General")
                            }
                            onPublicar(equiposFirebase)
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = nombres.size >= 3
                    ) {
                        Text("PUBLICAR TRIPLETAS", fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
