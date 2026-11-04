package com.acesso60.ui.preferences

/** Tamanho do texto escolhido pelo usuário (multiplica o ajuste de fonte do sistema). */
enum class TextScale(val factor: Float) {
    NORMAL(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f),
}
