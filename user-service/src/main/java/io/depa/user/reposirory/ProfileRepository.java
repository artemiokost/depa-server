package io.depa.user.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.user.model.Profile;
import io.depa.user.query.ProfileQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class ProfileRepository extends AsyncRepository<Profile> {

    public ProfileRepository() {
        super(Profile.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(ProfileQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long profileId) {
        Tuple arguments = Tuple.of(profileId);
        return delete(ProfileQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Profile>> findAll() {
        return findAll(ProfileQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Profile> findById(Long profileId) {
        Tuple arguments = Tuple.of(profileId);
        return find(ProfileQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Profile profile) {
        Tuple arguments = Arguments.builder()
                .add(profile.getUserId())
                .add(profile.getAbout())
                .add(profile.getBirthDate())
                .add(profile.getFullName())
                .add(profile.getGender())
                .add(profile.getImageUrl())
                .add(profile.getLocation())
                .add(profile.getCreatedAt())
                .add(profile.getUpdatedAt())
                .build();
        return save(ProfileQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long profileId, Profile newProfile) {
        return findById(profileId)
                .map(old -> old.merge(newProfile))
                .map(merged -> Arguments.builder()
                        .add(merged.getAbout())
                        .add(merged.getBirthDate())
                        .add(merged.getFullName())
                        .add(merged.getGender())
                        .add(merged.getImageUrl())
                        .add(merged.getLocation())
                        .add(merged.getUpdatedAt())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(ProfileQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<Profile> findByUserId(Long userId) {
        Tuple arguments = Tuple.of(userId);
        return find(ProfileQuery.SELECT_BY_USER_ID, arguments);
    }

    public Maybe<Profile> findByUsername(String username) {
        Tuple arguments = Tuple.of(username);
        return find(ProfileQuery.SELECT_BY_USERNAME, arguments);
    }
}
