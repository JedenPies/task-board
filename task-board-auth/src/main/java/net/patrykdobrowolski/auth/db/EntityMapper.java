package net.patrykdobrowolski.auth.db;

import net.patrykdobrowolski.auth.db.entity.UserEntity;
import net.patrykdobrowolski.auth.db.entity.UserTokenEntity;
import net.patrykdobrowolski.auth.domain.User;
import net.patrykdobrowolski.auth.domain.UserToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    User fromEntity(UserEntity userEntity);
    UserEntity toEntity(User user);
    UserTokenEntity toEntity(UserToken token);
    UserToken fromEntity(UserTokenEntity userTokenEntity);
}
