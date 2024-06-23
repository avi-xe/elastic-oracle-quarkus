package org.acme;

import java.io.IOException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@ApplicationScoped
@Path("/test")
public class TestService {
    @Inject
    EntityManager em;
    @Inject
    ElasticsearchClient elasticsearchClient;

    @Transactional
    @POST
    public Long createTest(TestEntity testEntity) throws ElasticsearchException, IOException {
        em.persist(testEntity);
        em.flush();
        IndexRequest<TestEntity> testIndexRequest = IndexRequest
                .of(t -> t.index("test").id(String.valueOf(testEntity.getId())).document(testEntity));
        elasticsearchClient.index(testIndexRequest);
        return testEntity.getId();
    }

    @Transactional
    @GET
    public TestEntity readTest(@QueryParam("id") Long id) {
        return em.find(TestEntity.class, id);
    }
}
