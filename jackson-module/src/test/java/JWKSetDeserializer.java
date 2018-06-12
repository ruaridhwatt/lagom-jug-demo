import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.Test;
import se.rwatt.jackson.module.JWKModule;

public class JWKSetDeserializer {

    @Test
    public void testDeserialize() throws Exception {
        String serialized = "{\"keys\":[{\"alg\":\"ES384\",\"crv\":\"P-384\"," +
                "\"kid\":\"punCqbtr5MwwmgwDMUE2KWkgnqCLvEkNqXzK6_pRWuQ\",\"kty\":\"EC\",\"use\":\"sig\"," +
                "\"x\":\"Tz7yKqoLm_AEbo9FfuQ2O5l_1kY7ahTgT4oJQ1qzWqoZgYtLrD-RH4f4Og5pPR0k\"," +
                "\"y\":\"wzXEATnQJCCjYjdYrTc2gZ7DZC_dUbcsNZ82-mTpfS0Lsu9MFbaK2-ZQxO7pX0Bh\"},{\"alg\":\"RS384\"," +
                "\"e\":\"AQAB\",\"kid\":\"8JlXLfg4Y8hCnqPDc1LeJfWLaQizTHLf7MAhHxbJixU\",\"kty\":\"RSA\"," +
                "\"n\":\"lzg7JwrUPITwZ2LhYNf4kKbjpvAEJRHoUA9Loa3OH6VmtSAwTEBJd5c3R1BinT2U_usrjVUqwStUlRXo9x9qq7HxaTCzXK" +
                "pI-0fRxfp9ph53-SuROo2UZM4uUP-Lemh4hjJ5Db5pgK_YJAs1d-SKv-KF4xjpGBz0tjl82dXqy-tEA_Z1naZCzhD9AY3SimNPB76U5" +
                "lgc9fRDmTACkoGTWkcmbFpjAFGEhbur-K3doSERZqHoFcTfGjBMywUHc4Y7rxSirTaI9stZvu-ROcKbpPdRJAZiBdtXwLOYWrjZY6wIQ" +
                "2fIig1WIsDCGiXt6F28_j8xMuiB7Yy6FY_v0aHtbQ\",\"use\":\"sig\"},{\"alg\":\"ES256\",\"crv\":\"P-256\",\"kid\":" +
                "\"UEgSjzfbxiGqK6PKz9oLDpjFMQUcmaqa8ykbnWonKJI\",\"kty\":\"EC\",\"use\":\"sig\",\"x\":\"qY7bImDrzLCFm-1kZhBu" +
                "cQtcvE2jh3z0AsfBM6suMME\",\"y\":\"eXawpKcre9n6BuITTfndxQNFIOojEEsUewOyLE79mIQ\"},{\"alg\":\"RS256\",\"e\":" +
                "\"AQAB\",\"kid\":\"BXgILTWqkQgQcNONe3uSFrlJswws9DBQCHz2K_UBIf4\",\"kty\":\"RSA\",\"n\":\"jnI87Q9y_1T8q1O-" +
                "JFf5Ffc8D4LLkV9qDL8ZmNSmn4fN6Sw3sRW6OBfm_7WFRhQs-OpcMYpsSAD3oa02p8dc9V-9VOAkR_vg5dYnxEOE3fML8YZI0zKKdKQij" +
                "wZZpXDJcE7H8h_b31Q55Bg2i1a45oVdsFV5MOsI7mZAn4-tluWPdX7y9ZoTpIaFayECv-ZeoY12CDuaSc2C4P2fJNA2hwGmVlejXQ5zcrRuI" +
                "sgzCA7DuniVDsJ2Ne4YINne5vujVdy1nUtGxkbOwhHVGgg6mOrcX5JJMt1c5Gqta1ljGbF4FWjUDM2n5gwkS3AjUpiO52CSdPqQk2JPbKzjKOW" +
                "iCQ\",\"use\":\"sig\"},{\"alg\":\"ES512\",\"crv\":\"P-521\",\"kid\":\"Npvuu0BgZbYKVaK-8j3MCda7-kkOiDEbK0am" +
                "_2XIXjo\",\"kty\":\"EC\",\"use\":\"sig\",\"x\":\"AKVksZ-O9vTHTk-q0h_d0T4BmZHfnR5qB9JB6bPtz2zdbpTdnMWzG3TPSx" +
                "o24G_icftn7NLiV39flu_dEVgNpmYI\",\"y\":\"ABsNvLLyrNIXaNEyzlxYvAbl8d-EZmlZH0vLS6QRqwc_7mtCvvPgqF3f-APMxOZH4" +
                "LMIKxJPiKz5Px3ixHsBOaBS\"},{\"alg\":\"RS512\",\"e\":\"AQAB\",\"kid\":\"cdP194NGJKND5KPaiyM7-OOvf8sPKy_ZAZgn" +
                "DHry2F4\",\"kty\":\"RSA\",\"n\":\"sRwEcUjTtiCNScsyI1qk58TP4_cZZ4JWDLFuKcipxOt_UXZITOsiFcbX0BTC0lx9V7wPGT0DjO" +
                "iJdhL6sCUW5TBE2ttyovs3f4CHZ6qpze88hQDuJUQlwfJRUdedupeOxsJ-_6iE5AwPDT2rNocSKy_DGojUYeHIGqX-ere2vP8FtoUD7547jDw" +
                "-mnuCM2H8GkevUtEwPPhqx3I2vXOqPEe4YPy6SlwCrYjRNZVYXm6JCA_BpcraDdve5rdVThXSGFRQBvDGlKv-fZjLUCNikwP5Uwscz7NRxXR" +
                "wAxndzu7nUuahMazXw1u7iyDkXlJLK4iZe8zm1L-w1BJkOcX5Dw\",\"use\":\"sig\"}]}";
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JWKModule());
        JWKSet jwkSet = om.readValue(serialized, JWKSet.class);
    }
}
