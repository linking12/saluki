package com.quancheng.saluki.gateway.oauth2.support;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class Oauth2UserStore {

    @Autowired
    private JdbcTemplate  jdbcTemplate;

    private MessageDigest messageDigest;

    public void init() {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }
    }

    public UserDetails loadUserByUsername(String username) {
        List<UserDetails> list = jdbcTemplate.query("select username,password,enabled from users where username = ?",
                                                    new String[] { username }, new RowMapper<UserDetails>() {

                                                        public UserDetails mapRow(ResultSet rs,
                                                                                  int rowNum) throws SQLException {
                                                            String username = rs.getString(1);
                                                            String password = rs.getString(2);
                                                            boolean enabled = rs.getBoolean(3);
                                                            return new Oauth2UserDetail(username, password, enabled);
                                                        }

                                                    });
        return list.get(0);
    }

    public String loadUsernameByToken(String token) {
        try {
            byte[] bytes = messageDigest.digest(token.getBytes("UTF-8"));
            String extractedToken = String.format("%032x", new BigInteger(1, bytes));
            return jdbcTemplate.queryForObject("select user_name from oauth_access_token where token_id = ?",
                                               new String[] { extractedToken }, String.class);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }

    }

}
