package domain.users;

import domain.enums.UserRole;

public interface IUser {
    String getUsername();

    String getPasswordHash();

    UserRole getRole();
}
