package med.voll.api.paciente;

import jakarta.validation.Valid;
import med.voll.api.endereco.DadosEndereco;

public record DadosListagemPaciente(Long id, String nome, String email, String cpf) {
    public DadosListagemPaciente(Paciente paciente) {
        this(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getCpf());
    }

}
