package com.engine.repository

import com.engine.model.DataType
import com.engine.model.MarketDataCache
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Explicitly scan ONLY the packages we need
@EntityScan("com.engine.model")
@EnableJpaRepositories("com.engine.repository")
// Bypass the main Application class by using a dummy config
@ContextConfiguration(classes = [MarketDataCacheRepository::class])
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:sqlite::memory:?cache=shared",
    "spring.datasource.driver-class-name=org.sqlite.JDBC",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
    "spring.jpa.show-sql=true",
    "fmp.api.key=test",     // Mocks the keys so the context doesn't fail
    "tiingo.api.key=test"
])
class MarketDataCacheRepositoryTest {
    @Autowired
    lateinit var repository: MarketDataCacheRepository

    @Test
    fun `should save and find market data by ticker, date, and type`() {
        // Arrange
        val date = LocalDate.now()
        val cacheEntry = MarketDataCache(
            ticker = "NVDA",
            date = date,
            value = BigDecimal("145.20"),
            type = DataType.PRICE,
            provider = "FMP"
        )

        // Act
        val saved = repository.save(cacheEntry)
        val found = repository.findByTickerAndDateAndType("NVDA", date, DataType.PRICE)

        // Assert
        assertThat(saved.id).isNotNull
        assertThat(found).isNotNull
        assertThat(found?.value).isEqualByComparingTo("145.20")
        assertThat(found?.ticker).isEqualTo("NVDA")
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // Force it to run without a managed transaction
    fun `should throw exception when saving duplicate ticker-date-type combination`() {
        val date = LocalDate.of(2026, 2, 4)
        val firstEntry = MarketDataCache(
            ticker = "AAPL", date = date, value = BigDecimal("180.00"),
            type = DataType.PRICE, provider = "FMP"
        )

        val duplicateEntry = MarketDataCache(
            ticker = "AAPL", date = date, value = BigDecimal("185.00"),
            type = DataType.PRICE, provider = "Tiingo"
        )

        // Save the first one
        repository.save(firstEntry)

        // Act & Assert: Attempting to save the second should trigger the unique constraint
        val exception = assertThrows<org.springframework.orm.jpa.JpaSystemException> {
            repository.saveAndFlush(duplicateEntry)
        }

        // Verify it's actually a constraint issue and not a random crash
        assertThat(exception.message).containsIgnoringCase("constraint").containsIgnoringCase("unique")
    }

    @Test
    fun `should return null when data does not exist in cache`() {
        // Act
        val found = repository.findByTickerAndDateAndType("NON_EXISTENT", LocalDate.now(), DataType.PRICE)

        // Assert
        assertThat(found).isNull()
    }
}
