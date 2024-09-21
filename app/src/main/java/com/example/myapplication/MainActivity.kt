package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.gson.Gson


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutMain()
        }
    }
}

class Estoque {
    companion object {
        private val listaProdutos = mutableListOf<Produto>()

        fun adicionarProduto(produto: Produto) {
            listaProdutos.add(produto)
        }

        fun calcularValorTotalEstoque(): Float {
            return listaProdutos.sumOf { it.preco * it.quantidade }
        }

        fun listarProdutos(): List<Produto> = listaProdutos
    }
}

@Composable
fun LayoutMain() {
    val navController = rememberNavController()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NavHost(navController = navController, startDestination = "telaCadastro") {
            composable("telaCadastro") { TelaCadastro(navController) }
            composable("telaLista") { TelaLista(navController) }
            composable("telaDetalhes/{produtoJSON}") { backStackEntry ->
                val produtoJSON = backStackEntry.arguments?.getString("produtoJSON")
                val produto = Gson().fromJson(produtoJSON, Produto::class.java)
                TelaDetalhes(produto, navController)
            }
            composable("telaEstatisticas") { TelaEstatisticas(navController) }
        }
    }
}

@Composable
fun TelaCadastro(navController: NavController) {
    var nome by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Produto") })
        TextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoria") })
        TextField(value = preco, onValueChange = { preco = it }, label = { Text("Preço") }, keyboardType = KeyboardType.Number)
        TextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade") }, keyboardType = KeyboardType.Number)

        Button(onClick = {
            val context = LocalContext.current
            if (nome.isBlank() || categoria.isBlank() || preco.isBlank() || quantidade.isBlank()) {
                Toast.makeText(context, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show()
            } else {
                val precoFloat = preco.toFloatOrNull() ?: 0f
                val quantidadeInt = quantidade.toIntOrNull() ?: 0

                // Validação conforme solicitado pelo professor
                if (quantidadeInt <= 0 || precoFloat <= 0) {
                    Toast.makeText(context, "Quantidade e preço devem ser maiores que 0", Toast.LENGTH_SHORT).show()
                } else {
                    Estoque.adicionarProduto(Produto(nome, categoria, precoFloat, quantidadeInt))
                    navController.navigate("telaLista")
                }
            }
        }) {
            Text("Cadastrar")
        }
    }
}

@Composable
fun TelaLista(navController: NavController) {
    val produtos = Estoque.listarProdutos()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LazyColumn {
            items(produtos) { produto ->
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            val produtoJSON = Gson().toJson(produto)
                            navController.navigate("telaDetalhes/$produtoJSON")
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${produto.nome} (${produto.quantidade} unidades)")
                    Button(onClick = { /* Detalhes do Produto */ }) {
                        Text("Detalhes")
                    }
                }
            }
        }

        Button(onClick = { navController.navigate("telaEstatisticas") }) {
            Text("Ver Estatísticas")
        }
    }
}

@Composable
fun TelaDetalhes(produto: Produto, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Nome: ${produto.nome}")
        Text(text = "Categoria: ${produto.categoria}")
        Text(text = "Preço: ${produto.preco}")
        Text(text = "Quantidade: ${produto.quantidade}")

        Button(onClick = { navController.popBackStack() }) {
            Text("Voltar")
        }
    }
}

@Composable
fun TelaEstatisticas(navController: NavController) {
    val valorTotal = Estoque.calcularValorTotalEstoque()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Valor Total do Estoque: $valorTotal")

        Button(onClick = { navController.popBackStack() }) {
            Text("Voltar")
        }
    }
}
}
