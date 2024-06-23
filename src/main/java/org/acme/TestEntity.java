package org.acme;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "test")
@NamedQuery(name = TestEntity.FIND_ALL, query = "from TestEntity order by id offset :offset rows fetch first :fetch_first rows only")
public class TestEntity {
    public final static String FIND_ALL = "TestEntity.FindAll";
    private Long id;
    private String name;
    private String description;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TestEntity [id=" + id + ", name=" + name + ", description=" + description + "]";
    }

}
