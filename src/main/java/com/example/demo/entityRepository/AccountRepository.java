
package com.example.demo.entityRepository;


import com.example.demo.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountRepository  extends JpaRepository<Account, Long> {

    Account findAccountById(Long id);
    Account findOneByEmail(String email);
    boolean existsByNome(String nome);

    @Transactional
    @Modifying
    @Query("update Account a set a.password = ?2 where a.id = ?1")
    void updatePassword(Long id, String newPassword);


    @Transactional
    @Modifying
    @Query("update Account a set a.password = ?2 where a.email = ?1")
    void updatePassword(String email, String newPassword);

    @Transactional
    void deleteById(Long id);

    @EntityGraph(attributePaths={"profilePicture"})
    Account findWithPropertyPictureAttachedById(Long id);

    @Transactional
    @Modifying
    @Query("update Account a set a.profilePicture = ?2 where a.id = ?1")
    void updateImage(Long id, byte[] newimg);

    @Query("SELECT a FROM Account a")
    List<Account> getAllAccount();

}

