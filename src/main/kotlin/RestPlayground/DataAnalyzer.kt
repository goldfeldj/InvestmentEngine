package RestPlayground

class DataAnalyzer {
    suspend fun averageItemValue(itemRepository: ItemRepository): Double =
        itemRepository.getAll().map { it.value }.sum()
}