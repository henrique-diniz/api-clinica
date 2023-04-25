package med.voll.api.paciente;

public record DTOListagemPaciente(Long id, String nome, String email, String cpf) {
    public DTOListagemPaciente(Paciente paciente) {
        this(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getCpf());
    }
}