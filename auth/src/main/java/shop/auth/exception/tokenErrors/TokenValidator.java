package shop.auth.exception.tokenErrors;

import shop.auth.enums.TokenError;
import shop.auth.model.Token;

import java.time.Instant;

public class TokenValidator {
    public void validate(Token token){

       if (token==null){
           throw new TokenValidationException(TokenError.TOKEN_NOT_FOUND);
       }

       if (token.isBlacklisted()){
           throw new TokenValidationException(TokenError.TOKEN_BLACKLISTED);
       }
       if (token.getExpiresAt().isBefore(Instant.now())){
           throw new TokenValidationException(TokenError.TOKEN_EXPIRED);
       }
    }
}
