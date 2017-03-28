package com.quancheng.saluki.gateway.oauth2.support;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    User findByUsernameCaseInsensitive(@Param("username") String username);

    @Query
    User findByEmail(String email);

    @Query
    User findByEmailAndActivationKey(String email, String activationKey);

    @Query
    User findByEmailAndResetPasswordKey(String email, String resetPasswordKey);

    @Query(value = "select * from  user u where exists(select user_name from oauth_access_token where user_name=u.username and  token_id=?1)", nativeQuery = true)
    User findByToken(String tokenId);

    @Query(value = "select authority from user_authority au where username=?1", nativeQuery = true)
    List<String> findUserAuthority(String userName);

}
