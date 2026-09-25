package com.example.tugasprak2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NewsCategory(val displayName: String) {
    ALL("Semua"),
    TECHNOLOGY("Teknologi"),
    SPORTS("Olahraga"),
    ECONOMY("Ekonomi")
}

data class News(
    val id: Int,
    val title: String,
    val category: NewsCategory,
    val content: String
)

sealed interface NewsDetailUiState {
    data object Idle : NewsDetailUiState
    data object Loading : NewsDetailUiState
    data class Success(val news: News, val detailText: String) : NewsDetailUiState
}

class NewsRepository {

    private val newsData = listOf(
        News(1, "Teknologi AI Semakin Berkembang", NewsCategory.TECHNOLOGY, "Perkembangan kecerdasan buatan semakin pesat dan digunakan dalam berbagai bidang."),
        News(2, "Tim Nasional Bersiap Menghadapi Pertandingan", NewsCategory.SPORTS, "Tim nasional melakukan persiapan menjelang pertandingan berikutnya."),
        News(3, "Harga Komoditas Mengalami Perubahan", NewsCategory.ECONOMY, "Perubahan harga komoditas menjadi perhatian bagi masyarakat dan pelaku usaha."),
        News(4, "Smartphone Baru Resmi Diluncurkan", NewsCategory.TECHNOLOGY, "Perusahaan teknologi meluncurkan smartphone dengan berbagai fitur baru."),
        News(5, "Pertandingan Berakhir dengan Skor Ketat", NewsCategory.SPORTS, "Pertandingan berlangsung sengit hingga akhir permainan."),
        News(6, "Perkembangan Ekonomi Digital Indonesia", NewsCategory.ECONOMY, "Ekonomi digital terus mengalami perkembangan di Indonesia."),
        News(7, "Aplikasi Baru Membantu Produktivitas", NewsCategory.TECHNOLOGY, "Sebuah aplikasi baru dikembangkan untuk membantu meningkatkan produktivitas."),
        News(8, "Kompetisi Olahraga Tingkat Nasional", NewsCategory.SPORTS, "Kompetisi olahraga tingkat nasional kembali diselenggarakan.")
    )

    fun getNewsStream(): Flow<News> = flow {
        for (news in newsData) {
            delay(2000L)
            emit(news)
        }
    }

    suspend fun fetchNewsDetail(news: News): String {
        delay(1000L)
        return """
            ${news.title}
            
            Kategori:
            ${news.category.displayName}
            
            Detail:
            ${news.content}
        """.trimIndent()
    }
}

class NewsViewModel(
    private val repository: NewsRepository = NewsRepository()
) : ViewModel() {

    private val _readCount = MutableStateFlow(0)
    val readCount: StateFlow<Int> = _readCount.asStateFlow()

    private val _newsList = MutableStateFlow<List<News>>(emptyList())

    private val _selectedCategory = MutableStateFlow(NewsCategory.ALL)
    val selectedCategory: StateFlow<NewsCategory> = _selectedCategory.asStateFlow()

    private val _detailUiState = MutableStateFlow<NewsDetailUiState>(NewsDetailUiState.Idle)
    val detailUiState: StateFlow<NewsDetailUiState> = _detailUiState.asStateFlow()

    val filteredNews: StateFlow<List<News>> = combine(
        _newsList,
        _selectedCategory
    ) { newsList, category ->
        if (category == NewsCategory.ALL) {
            newsList
        } else {
            newsList.filter { it.category == category }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        startNewsSimulation()
    }

    private fun startNewsSimulation() {
        viewModelScope.launch {
            repository.getNewsStream().collect { newArticle ->
                _newsList.update { currentList ->
                    if (currentList.none { it.id == newArticle.id }) {
                        currentList + newArticle
                    } else {
                        currentList
                    }
                }
            }
        }
    }

    fun setCategory(category: NewsCategory) {
        _selectedCategory.value = category
    }

    fun readNews(news: News) {
        _readCount.update { it + 1 }

        viewModelScope.launch {
            _detailUiState.value = NewsDetailUiState.Loading
            val detail = repository.fetchNewsDetail(news)
            _detailUiState.value = NewsDetailUiState.Success(news, detail)
        }
    }

    fun closeDetail() {
        _detailUiState.value = NewsDetailUiState.Idle
    }
}

@Composable
fun App() {
    MaterialTheme {
        NewsFeedApp()
    }
}

@Composable
fun NewsFeedApp(
    viewModel: NewsViewModel = viewModel { NewsViewModel() }
) {
    val filteredNews by viewModel.filteredNews.collectAsStateWithLifecycle()
    val readCount by viewModel.readCount.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val detailUiState by viewModel.detailUiState.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "News Feed Simulator",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Berita sudah dibaca: $readCount",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Filter: ${selectedCategory.displayName}")

            Box {
                Button(onClick = { showMenu = true }) {
                    Text("Pilih")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    NewsCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                viewModel.setCategory(category)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = filteredNews,
                key = { it.id }
            ) { news ->
                NewsCard(
                    news = news,
                    onRead = { viewModel.readNews(news) }
                )
            }
        }

        when (val state = detailUiState) {
            is NewsDetailUiState.Idle -> {}
            is NewsDetailUiState.Loading -> {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is NewsDetailUiState.Success -> {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = state.detailText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.closeDetail() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Tutup")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    news: News,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Kategori: ${news.category.displayName}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onRead) {
                Text("Baca Berita")
            }
        }
    }
}
