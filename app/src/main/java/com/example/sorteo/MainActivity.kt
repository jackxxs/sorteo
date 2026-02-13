package com.example.sorteo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
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
            VistaJugador { userRole = UserRole.NONE }
        }
        UserRole.CREATOR -> {
            VistaCreador { userRole = UserRole.NONE }
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
        Text("PETANCA LIVE ⚾", fontWeight = FontWeight.Black, fontSize = 28.sp)
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
                fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp)
            )
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
                    Text("No hay jugadores añadidos", Modifier.align(Alignment.Center), color = Color.LightGray)
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
                            HorizontalDivider(color = Color(0xFFF1F1F1))
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
fun VistaJugador(onBack: () -> Unit) {
    val firebaseManager = remember { FirebaseManager() }
    val equiposInternet by firebaseManager.escucharSorteo().collectAsState(initial = emptyList())
    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val fases = listOf("General", "Consolación", "Repesca")

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("PETANCA LIVE ⚾", fontWeight = FontWeight.Black, fontSize = 20.sp)
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
            }
        }

        if (equiposInternet.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("El torneo aún no ha comenzado.", fontSize = 16.sp, color = Color.Gray)
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
                val esUltimoEquipo = filtrados.size == 1

                itemsIndexed(partidas) { _, pareja ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            val eqA = pareja[0]
                            val indexA = equiposInternet.indexOf(eqA)
                            val eqB = if (pareja.size > 1) pareja[1] else null
                            val indexB = if (eqB != null) equiposInternet.indexOf(eqB) else -1

                            EquipoCardEnfrentamiento(
                                equipo = eqA,
                                rival = eqB,
                                esElUnico = esUltimoEquipo,
                                onReclamar = { firebaseManager.reclamarVictoria(indexA) },
                                onConfirmarDerrota = { if (indexB != -1) firebaseManager.confirmarResultado(indexB, indexA, faseActual) },
                                onNegarVictoria = { if (indexB != -1) firebaseManager.rechazarVictoria(indexB) },
                                onPasarDescanso = { firebaseManager.confirmarResultado(indexA, null, faseActual) },
                                onGanarTorneo = { firebaseManager.proclamarCampeon(indexA) }
                            )

                            if (eqB != null) {
                                Text("VS", Modifier.fillMaxWidth().padding(vertical = 4.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                EquipoCardEnfrentamiento(
                                    equipo = eqB,
                                    rival = eqA,
                                    esElUnico = false,
                                    onReclamar = { if (indexB != -1) firebaseManager.reclamarVictoria(indexB) },
                                    onConfirmarDerrota = { firebaseManager.confirmarResultado(indexA, indexB, faseActual) },
                                    onNegarVictoria = { firebaseManager.rechazarVictoria(indexA) },
                                    onPasarDescanso = {},
                                    onGanarTorneo = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VistaCreador(onBack: () -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { SorteoDataStore(context) }
    val nombres by dataStore.nombres.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    val firebaseManager = remember { FirebaseManager() }
    var isAdmin by remember { mutableStateOf(false) }
    var claveInput by remember { mutableStateOf("") }
    var nombreTexto by remember { mutableStateOf("") }

    val equiposInternet by firebaseManager.escucharSorteo().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
                Text("Creador del Torneo", fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp))
            }
            if (isAdmin) {
                Surface(color = Color(0xFFC8E6C9), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "ADMIN",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (isAdmin) {
            if (equiposInternet.isEmpty()) {
                SelectorAdmin(
                    nombres = nombres,
                    onNombresChange = { nuevosNombres ->
                        coroutineScope.launch {
                            dataStore.guardarNombres(nuevosNombres)
                        }
                    },
                    onTextoChange = { nombreTexto = it },
                    texto = nombreTexto,
                    onStart = { modoSelected ->
                        val tamano = if (modoSelected == "dupletas") 2 else 3
                        val listaObjetos = nombres.shuffled().chunked(tamano).map { EquipoFirebase(miembros = it, fase = "General") }
                        firebaseManager.publicarSorteo(listaObjetos)
                        coroutineScope.launch {
                            dataStore.guardarNombres(emptyList())
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("El torneo ya está en marcha.")
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                dataStore.guardarNombres(emptyList())
                            }
                            firebaseManager.publicarSorteo(emptyList())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) { Text("BORRAR TORNEO Y LISTA") }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Introduce la clave de administrador",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    OutlinedTextField(
                        value = claveInput,
                        onValueChange = { claveInput = it },
                        label = { Text("Clave") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { if (claveInput == "1234") isAdmin = true }) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Login")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectorAdmin(
    nombres: List<String>,
    onNombresChange: (List<String>) -> Unit,
    onTextoChange: (String) -> Unit,
    texto: String,
    onStart: (String) -> Unit,
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
                    Text("No hay jugadores añadidos", Modifier.align(Alignment.Center), color = Color.LightGray)
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
                            HorizontalDivider(color = Color(0xFFF1F1F1))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStart("dupletas") }, Modifier.weight(1f), enabled = nombres.size >= 2, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("INICIAR DUO") }
                Button(onClick = { onStart("tripletas") }, Modifier.weight(1f), enabled = nombres.size >= 3, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("INICIAR TRIO") }
            }
        }
    }
}

@Composable
fun PantallaResultadoSorteo(resultado: List<List<String>>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(all = 12.dp)) {
        itemsIndexed(resultado) { index, equipo ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Equipo ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    equipo.forEach {
                        Text("• $it", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EquipoCardEnfrentamiento(
    equipo: EquipoFirebase,
    rival: EquipoFirebase?,
    esElUnico: Boolean,
    onReclamar: () -> Unit,
    onConfirmarDerrota: () -> Unit,
    onNegarVictoria: () -> Unit,
    onPasarDescanso: () -> Unit,
    onGanarTorneo: () -> Unit
) {
    val naranja = Color(0xFFFFA500)
    val verdeOk = Color(0xFF2E7D32)
    val azulDescanso = Color(0xFF1976D2)
    val oroCampeon = Color(0xFFFFD700)

    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = if (equipo.ganador) Color(0xFFE8F5E9) else Color.White),
        shape = RoundedCornerShape(8.dp),
        border = if (equipo.reclamandoVictoria) BorderStroke(2.dp, naranja) else if (equipo.ganador) BorderStroke(2.dp, oroCampeon) else null
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(equipo.miembros.joinToString(" • "), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (equipo.ganador) {
                    Text("🏆 ¡CAMPEÓN!", fontSize = 12.sp, color = verdeOk, fontWeight = FontWeight.ExtraBold)
                } else if (equipo.haDescansado) {
                    Text("YA HA DESCANSADO", fontSize = 9.sp, color = Color.Gray)
                }
            }

            if (!equipo.ganador) {
                if (esElUnico && rival == null) {
                    Button(onClick = onGanarTorneo, colors = ButtonDefaults.buttonColors(containerColor = oroCampeon)) {
                        Icon(Icons.Default.EmojiEvents, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Text("GANAR", fontSize = 10.sp, color = Color.Black)
                    }
                } else if (rival == null) {
                    Button(onClick = onPasarDescanso, colors = ButtonDefaults.buttonColors(containerColor = azulDescanso)) {
                        Text("DESCANSAR", fontSize = 10.sp)
                    }
                } else {
                    if (rival.reclamandoVictoria) {
                        Row {
                            Button(
                                onClick = onConfirmarDerrota,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("PERDÍ", fontSize = 10.sp)
                            }
                            Spacer(Modifier.width(4.dp))
                            OutlinedButton(onClick = onNegarVictoria, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text("FALSO", fontSize = 10.sp)
                            }
                        }
                    } else if (!equipo.reclamandoVictoria) {
                        IconButton(onClick = onReclamar) {
                            Icon(Icons.Default.CheckCircle, null, tint = verdeOk)
                        }
                    }
                }
            }
        }
    }
}
