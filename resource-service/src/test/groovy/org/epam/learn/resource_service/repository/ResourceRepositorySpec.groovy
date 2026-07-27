package org.epam.learn.resource_service.repository

import org.epam.learn.resource_service.model.Mp3FileUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = [
        "eureka.client.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.jpa.hibernate.ddl-auto=create-drop"
])
class ResourceRepositorySpec extends Specification {

    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17").tap { start() }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl)
        registry.add("spring.datasource.username", POSTGRES::getUsername)
        registry.add("spring.datasource.password", POSTGRES::getPassword)
    }

    @TestConfiguration
    static class NoOpS3Init {
        @Bean
        ApplicationRunner createBucket() {
            return { args -> } as ApplicationRunner
        }
    }

    @Autowired
    ResourceRepository repository

    def "save and retrieve resource by id"() {
        given: "a new resource entity with an S3 location"
        def entity = new Mp3FileUrl(UUID.randomUUID(), "s3://bucket/track-1.mp3")

        when: "the entity is persisted"
        def saved = repository.save(entity)

        then: "it can be found by the generated id"
        repository.findById(saved.id).isPresent()
        repository.findById(saved.id).get().url == "s3://bucket/track-1.mp3"
    }

    def "deleteAllById removes only the requested id"() {
        given: "two persisted entities"
        def a = repository.save(new Mp3FileUrl(UUID.randomUUID(), "s3://bucket/a.mp3"))
        def b = repository.save(new Mp3FileUrl(UUID.randomUUID(), "s3://bucket/b.mp3"))

        when: "only the first id is deleted"
        repository.deleteAllById([a.id])

        then: "the first entity is gone but the second remains"
        repository.findById(a.id).isEmpty()
        repository.findById(b.id).isPresent()
    }
}
