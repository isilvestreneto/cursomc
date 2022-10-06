package dev.silvestre.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import dev.silvestre.cursomc.domain.Categoria;
import dev.silvestre.cursomc.dto.CategoriaDTO;
import dev.silvestre.cursomc.repositories.CategoriaRepository;
import dev.silvestre.cursomc.services.exceptions.DataIntegrityException;
import dev.silvestre.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class CategoriaService {

	ModelMapper mapper = new ModelMapper();

	@Autowired
	private CategoriaRepository repo;

	public Categoria find(Integer id) {
		Optional<Categoria> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Categoria.class.getName()));
	}

	public List<Categoria> findAll() {

		return repo.findAll();
	}

	public Categoria insert(Categoria obj) {
		obj.setId(null);
		return repo.save(obj);
	}

	public Categoria update(Categoria obj) {

		Categoria newObj = find(obj.getId());

		updateData(newObj, obj);

		return repo.save(newObj);
	}

	private void updateData(Categoria newObj, Categoria obj) {

		newObj.setNome(obj.getNome());

	}

	public void delete(Integer id) {
		find(id);

		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir uma categoria que possui produtos");
		}
	}

	public Page<Categoria> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	public Categoria fromDTO(CategoriaDTO categoriaDto) {

		return mapper.map(categoriaDto, Categoria.class);
	}

}
