package noctem.alertServer.global.common;

import io.fusionauth.jwt.InvalidJWTException;
import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

@Slf4j
@Component
public class JwtDataExtractor {
    public final String JWT_HEADER = "Authorization";
    public final String JWT_SIGNER = System.getenv("NOCTEM_JWT_SIGNER");
    public final String JWT_ISSUER = "Cafe Noctem";
    public final String JWT_USER_ACCOUNT_ID = "userAccountId";
    public final String JWT_NICKNAME = "nickname";
    public final String JWT_STORE_ID = "storeId";

    public Long extractStoreId(ServerWebExchange exchange) {
        JWT decodedJwt = extractDecodedJwt(exchange);
        if (decodedJwt == null) {
            throw CommonException.builder().build();
        }
        return (Long) decodedJwt.getAllClaims().get(JWT_STORE_ID);
    }

    public Long extractUserAccountId(ServerWebExchange exchange) {
        JWT decodedJwt = extractDecodedJwt(exchange);
        if (decodedJwt == null) {
            throw CommonException.builder().build();
        }
        return (Long) decodedJwt.getAllClaims().get(JWT_USER_ACCOUNT_ID);
    }

    private JWT extractDecodedJwt(ServerWebExchange exchange) {
        // 토큰 유효성 검사
        HttpHeaders headers = exchange.getRequest().getHeaders();
        if (!headers.containsKey(JWT_HEADER)
                || headers.get(JWT_HEADER).size() != 1
                || headers.get(JWT_HEADER).get(0) == null) {
            log.warn("Non-exist JWT");
            throw CommonException.builder().build();
        }
        String jwt = headers.get(JWT_HEADER).get(0);
        try {
            String encodedJwt = jwt.split(" ")[1];
            Verifier verifier = HMACVerifier.newVerifier(JWT_SIGNER);
            JWT decodedJwt = JWT.getDecoder().decode(encodedJwt, verifier);

            Map<String, Object> allClaims = decodedJwt.getAllClaims();

            if (!allClaims.get("iss").toString().equals(JWT_ISSUER)) {
                throw new InvalidJWTSignatureException();
            }
            return decodedJwt;
        } catch (InvalidJWTSignatureException e) {
            log.warn("InvalidJWTSignatureException");
            throw CommonException.builder().build();
        } catch (NullPointerException npe) {
            log.warn("NPE: {}", npe.getMessage());
        } catch (JWTExpiredException jwtEx) {
            log.warn("Expired Token, class: {}", jwtEx.getClass());
        } catch (InvalidJWTException exception) {
            log.warn("Invalid Token, class: {}", exception.getClass());
        } catch (ArrayIndexOutOfBoundsException exception) {
            log.warn("Invalid Token, class: {}", exception.getClass());
        } catch (Exception exception) {
            log.error("Message: {}, class: {}", exception.getMessage(), exception.getClass());
        }
        return null;
    }
}
