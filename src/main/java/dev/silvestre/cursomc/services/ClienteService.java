package dev.silvestre.cursomc.services;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import dev.silvestre.cursomc.domain.Cidade;
import dev.silvestre.cursomc.domain.Cliente;
import dev.silvestre.cursomc.domain.Endereco;
import dev.silvestre.cursomc.domain.enums.TipoCliente;
import dev.silvestre.cursomc.dto.ClienteDTO;
import dev.silvestre.cursomc.dto.ClienteNewDTO;
import dev.silvestre.cursomc.repositories.ClienteRepository;
import dev.silvestre.cursomc.repositories.EnderecoRepository;
import dev.silvestre.cursomc.services.exceptions.DataIntegrityException;
import dev.silvestre.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	ModelMapper mapper = new ModelMapper();

	@Autowired
	private ClienteRepository repo;

	@Autowired
	private EnderecoRepository enderecoRepo;

	public List<Cliente> findAll() {

		return repo.findAll();
	}

	public Cliente find(Integer id) {
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepo.saveAll(obj.getEnderecos());
		
		return obj;
	}

	public Cliente update(Cliente obj) {

		Cliente newObj = find(obj.getId());

		updateData(newObj, obj);

		return repo.save(newObj);
	}

	private void updateData(Cliente newObj, Cliente obj) {

		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());

	}

	public void delete(Integer id) {
		find(id);

		try {
			repo.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há pedidos relacionados");
		}
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	public Cliente fromDTO(ClienteDTO clienteDto) {

		return new Cliente(clienteDto.getId(), clienteDto.getNome(), clienteDto.getEmail(), null, null);
	}

	public Cliente fromDTO(ClienteNewDTO clienteDto) {
		Cliente cli = new Cliente(null, clienteDto.getNome(), clienteDto.getEmail(), clienteDto.getCpfOuCnpj(),
				TipoCliente.toEnum(clienteDto.getTipo()));

		Cidade cid = new Cidade(clienteDto.getCidadeId(), null, null);

		Endereco end = new Endereco(null, clienteDto.getLogradouro(), clienteDto.getNumero(),
				clienteDto.getComplemento(), clienteDto.getBairro(), clienteDto.getCep(), cli, cid);

		cli.getEnderecos().add(end);
		cli.getTelefones().add(clienteDto.getTelefone1());

		if (clienteDto.getTelefone2() != null) {
			cli.getTelefones().add(clienteDto.getTelefone2());
		}
		if (clienteDto.getTelefone3() != null) {
			cli.getTelefones().add(clienteDto.getTelefone3());
		}

		return cli;
	}

}
