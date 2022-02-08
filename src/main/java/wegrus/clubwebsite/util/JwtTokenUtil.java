package wegrus.clubwebsite.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.exception.RefreshTokenExpiredException;
import wegrus.clubwebsite.exception.JwtInvalidException;

import java.util.*;
import java.util.function.Function;

import static wegrus.clubwebsite.dto.error.ErrorCode.INVALID_JWT;

@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.access.validity}")
    private long ACCESS_TOKEN_VALIDITY;
    @Value("${jwt.refresh.validity}")
    private long REFRESH_TOKEN_VALIDITY;

    @Value("${jwt.access.secret}")
    private String ACCESS_TOKEN_SECRET;
    @Value("${jwt.refresh.secret}")
    private String REFRESH_TOKEN_SECRET;

    public String getUsernameFromAccessToken(String token) {
        return getClaimFromAccessToken(token, Claims::getSubject);
    }

    public String getUsernameFromRefreshToken(String token) {
        return getClaimFromRefreshToken(token, Claims::getSubject);
    }

    public <T> T getClaimFromAccessToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromAccessToken(token);
        return claimsResolver.apply(claims);
    }

    public <T> T getClaimFromRefreshToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromRefreshToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromAccessToken(String token) {
        return Jwts.parser().setSigningKey(ACCESS_TOKEN_SECRET).parseClaimsJws(token).getBody();
    }

    private Claims getAllClaimsFromRefreshToken(String token) {
        return Jwts.parser().setSigningKey(REFRESH_TOKEN_SECRET).parseClaimsJws(token).getBody();
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "AccessToken");
        return doGenerateToken(claims, headers, userDetails.getUsername(), ACCESS_TOKEN_VALIDITY, ACCESS_TOKEN_SECRET);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "RefreshToken");
        return doGenerateToken(claims, headers, userDetails.getUsername(), REFRESH_TOKEN_VALIDITY, REFRESH_TOKEN_SECRET);
    }

    private String doGenerateToken(Map<String, Object> claims, Map<String, Object> headers, String subject, long validity, String secret) {
        return Jwts.builder()
                .setHeader(headers)
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(ACCESS_TOKEN_SECRET).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            final List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("Authorization", "Bearer " + token, INVALID_JWT.getMessage()));
            throw new JwtInvalidException(errors);
        }
        return true;
    }

    public void validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(REFRESH_TOKEN_SECRET).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new RefreshTokenExpiredException();
        } catch (JwtException e) {
            final List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("refreshToken", token, INVALID_JWT.getMessage()));
            throw new JwtInvalidException(errors);
        }
    }
}
