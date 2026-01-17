package com.zyloerp.modules.auth.repository;

import com.zyloerp.modules.cliente.model.Cliente;
import com.zyloerp.shared.enums.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {


    Optional<Cliente> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    @Query("SELECT c FROM Cliente c WHERE c.excluidoEm IS NULL ORDER BY c.razaoSocial")
    List<Cliente> findAllAtivos();

    @Query("SELECT c FROM Cliente c WHERE c.statusCliente = :status AND c.excluidoEm IS NULL")
    List<Cliente> findByStatus(@Param("status") StatusCliente status);

    List<Cliente> findByCidadeAndEstadoAndExcluidoEmIsNull(String cidade, String estado);

    @Query("SELECT c FROM Cliente c WHERE " +
            "(LOWER(c.razaoSocial) LIKE LOWER(CONCAT('%', :nome, '%')) OR " +
            "LOWER(c.nomeFantasia) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
            "AND c.excluidoEm IS NULL")
    List<Cliente> findByNome(@Param("nome") String nome);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.statusCliente = :status AND c.excluidoEm IS NULL")
    Long countByStatus(@Param("status") StatusCliente status);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.contatos WHERE c.codigoCliente = :codigo")
    Optional<Cliente> findByIdComContatos(@Param("codigo") Long codigo);
}
