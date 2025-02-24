package org.beerbower.vanlife.controllers;

import org.beerbower.vanlife.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Test
    public void testRequiredArgsConstructor() {
        UserRepository mockRepository = mock(UserRepository.class);
        UserController userController = new UserController(mockRepository);

        assertNotNull(userController);
    }
}
