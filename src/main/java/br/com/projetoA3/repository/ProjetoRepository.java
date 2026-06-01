Set-Content -Path "src\main\java\br\com\projetoA3\repository\ProjetoRepository.java" -Value @"
package br.com.projetoA3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.projetoA3.entity.Projeto;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

}
"@
