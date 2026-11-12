const HORA_INICIO_TARDE = 12;
const HORA_INICIO_NOITE = 18;
const HORA_INICIO_MANHA = 5;

export function saudacaoPorHora(hora: number): string {
  if (hora >= HORA_INICIO_MANHA && hora < HORA_INICIO_TARDE) {
    return 'Bom dia';
  }
  if (hora >= HORA_INICIO_TARDE && hora < HORA_INICIO_NOITE) {
    return 'Boa tarde';
  }
  return 'Boa noite';
}
