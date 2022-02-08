package wegrus.clubwebsite.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import wegrus.clubwebsite.exception.AuthorizationHeaderInvalidException;
import wegrus.clubwebsite.util.JwtUserDetailsUtil;
import wegrus.clubwebsite.util.JwtTokenUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static wegrus.clubwebsite.dto.error.ErrorCode.*;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUserDetailsUtil jwtUserDetailsUtil;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestTokenHeader = request.getHeader("Authorization");

        try {
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer "))
                throw new AuthorizationHeaderInvalidException();
            String jwtToken = requestTokenHeader.substring(7);
            String id = jwtTokenUtil.getUsernameFromAccessToken(jwtToken);

            if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.jwtUserDetailsUtil.loadUserByUsername(id);

                if (jwtTokenUtil.validateAccessToken(jwtToken)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } catch (AuthorizationHeaderInvalidException e) {
            request.setAttribute("errorCode", INVALID_AUTHORIZATION_HEADER);
        } catch (ExpiredJwtException e) {
            request.setAttribute("errorCode", EXPIRED_ACCESS_TOKEN);
        } catch (JwtException e) {
            request.setAttribute("errorCode", INVALID_JWT);
        }

        filterChain.doFilter(request, response);
    }
}
