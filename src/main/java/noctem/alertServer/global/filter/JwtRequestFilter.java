package noctem.alertServer.global.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.global.common.SinkSessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter implements WebFilter {
    private final SinkSessionRegistry sinkSessionRegistry;
    public final String JWT_HEADER = "Authorization";
    public final String JWT_SIGNER = System.getenv("NOCTEM_JWT_SIGNER");
    public final String JWT_ISSUER = "Cafe Noctem";
    public final String JWT_USER_ACCOUNT_ID = "userAccountId";
    public final String JWT_NICKNAME = "nickname";
    public final String JWT_STORE_ID = "storeId";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 토큰 유효성 검사
//        HttpHeaders headers = exchange.getRequest().getHeaders();
//        if (!headers.containsKey(JWT_HEADER)
//                || headers.get(JWT_HEADER).size() != 1
//                || headers.get(JWT_HEADER).get(0) == null) {
//            log.warn("Non-exist JWT");
//            throw CommonException.builder().build();
//        }
//        String jwt = headers.get(JWT_HEADER).get(0);
//        try {
//            String encodedJwt = jwt.split(" ")[1];
//            Verifier verifier = HMACVerifier.newVerifier(JWT_SIGNER);
//            JWT decodedJwt = JWT.getDecoder().decode(encodedJwt, verifier);
//
//            Map<String, Object> allClaims = decodedJwt.getAllClaims();
//
//            if (!allClaims.get("iss").toString().equals(JWT_ISSUER)) {
//                throw new InvalidJWTSignatureException();
//            }
//            return chain.filter(exchange);
//        } catch (InvalidJWTSignatureException e) {
//            log.warn("InvalidJWTSignatureException");
//            throw CommonException.builder().build();
//        } catch (NullPointerException npe) {
//            log.warn("NPE: {}", npe.getMessage());
//        } catch (JWTExpiredException jwtEx) {
//            log.warn("Expired Token, class: {}", jwtEx.getClass());
//        } catch (InvalidJWTException exception) {
//            log.warn("Invalid Token, class: {}", exception.getClass());
//        } catch (ArrayIndexOutOfBoundsException exception) {
//            log.warn("Invalid Token, class: {}", exception.getClass());
//        } catch (Exception exception) {
//            log.error("Message: {}, class: {}", exception.getMessage(), exception.getClass());
//        }
//        return null;
        return chain.filter(exchange);
    }
}
