package med.voll.api.paciente;

import jakarta.validation.Valid;
import med.voll.api.endereco.DadosEndereco;

public record DTOAtualizacaoPaciente (
        Long id,
        String nome,
        String telefone,
        DadosEndereco endereco
) {
}