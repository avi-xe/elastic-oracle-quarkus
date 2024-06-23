package org.acme;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.logging.Logger;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
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

    private static final Logger log = Logger.getLogger(TestService.class);
    private final Executor executor = Executors.newFixedThreadPool(8);
    private final BulkRequest.Builder builder = new BulkRequest.Builder();
    private Map<UUID, CompletableFuture<Void>> futures = new HashMap<>();

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
        TestEntity testEntity = em.find(TestEntity.class, id);
        log.info("found: " + testEntity.toString());
        return testEntity;
    }

    @Transactional
    @GET
    @Path("/bulk")
    public String bulkUpload(@QueryParam("bulk_size") Long bulkSize, @QueryParam("schedule") String schedule) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> bulkUploadRequest(bulkSize), executor).thenAccept(t -> log.info(t));
        UUID randomUUID = UUID.randomUUID();
        futures.put(randomUUID, future);

        return randomUUID.toString();
    }

    @Transactional
    public void bulkUploadRequest(Long bulkSize) {
        log.info("hello from the future");
        List<TestEntity> resultList;
        Long offset = 0L;

        try {
            TypedQuery<TestEntity> namedQuery = em.createNamedQuery(TestEntity.FIND_ALL, TestEntity.class)
                    .setParameter("offset", offset).setParameter("fetch_first", bulkSize);
            resultList = namedQuery.getResultList();
            log.info(resultList);
            for (TestEntity result : resultList) {
                log.info("read: " + result.toString());
                builder.operations(
                        op -> op.index(idx -> idx.index("test").id(result.getId().toString()).document(result)));
            }
            try {
                elasticsearchClient.bulk(builder.build());
            } catch (ElasticsearchException e) {
                log.error(e);
                e.printStackTrace();
            } catch (IOException e) {
                log.error(e);
            }
            offset += bulkSize;
        } catch (Exception e) {
            log.warn(e);
        }
    }

    @Transactional
    @DELETE
    @Path("/bulk")
    public Boolean bulkUploadStop(@QueryParam("uuid") String uuid) {
        UUID fromString = UUID.fromString(uuid);
        CompletableFuture<Void> completableFuture = futures.get(fromString);
        if (completableFuture.cancel(true)) {
            futures.remove(fromString);
            return true;
        }
        return false;
    }
}
