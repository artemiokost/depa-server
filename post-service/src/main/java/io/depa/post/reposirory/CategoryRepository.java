package io.depa.post.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Category;
import io.depa.post.query.CategoryQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class CategoryRepository extends AsyncRepository<Category> {

    public CategoryRepository() {
        super(Category.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(CategoryQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long categoryId) {
        Tuple arguments = Tuple.of(categoryId);
        return delete(CategoryQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Category>> findAll() {
        return findAll(CategoryQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Category> findById(Long categoryId) {
        Tuple arguments = Tuple.of(categoryId);
        return find(CategoryQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Category category) {
        Tuple arguments = Tuple.of(category.getName());
        return save(CategoryQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long categoryId, Category newCategory) {
        return findById(categoryId)
                .map(old -> old.merge(newCategory))
                .map(merged -> Tuple.of(merged.getName(), merged.getId(), merged.getId()))
                .flatMapCompletable(arguments -> update(CategoryQuery.UPDATE_BY_ID, arguments));
    }
}
