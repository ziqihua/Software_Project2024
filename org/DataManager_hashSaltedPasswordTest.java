import static org.junit.Assert.*;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

public class DataManager_hashSaltedPasswordTest {
    @Test
    public void testGoodPath() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        String password = "myPassword";
        String result = dm.hashSaltedPassword(password);
        String expected = "185859aad100020706b4ec4c1c130aa816e3b1906e7b94006755e92be3215daa";
        assertEquals(expected, result);
    }

    @Test public void testDifferentPasswords() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        String password1 = "myPassword1";
        String password2 = "myPassword2";
        String result1 = dm.hashSaltedPassword(password1);
        String result2 = dm.hashSaltedPassword(password2);
        assert(!result1.equals(result2));
    }

    @Test (expected = RuntimeException.class)
    public void testBadHashAlg() {
        DataManager dm = new DataManager(new WebClient("localhost", 3001));
        dm.HASH_ALGORITHM = "bad_alg";
        String password1 = "myPassword1";
        String result1 = dm.hashSaltedPassword(password1);
    }
}