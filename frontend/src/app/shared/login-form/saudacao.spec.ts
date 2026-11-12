import { saudacaoPorHora } from './saudacao';

describe('saudacaoPorHora', () => {
  it('deve devolver Bom dia pela manhã', () => {
    expect(saudacaoPorHora(5)).toBe('Bom dia');
    expect(saudacaoPorHora(11)).toBe('Bom dia');
  });

  it('deve devolver Boa tarde à tarde', () => {
    expect(saudacaoPorHora(12)).toBe('Boa tarde');
    expect(saudacaoPorHora(17)).toBe('Boa tarde');
  });

  it('deve devolver Boa noite à noite e na madrugada', () => {
    expect(saudacaoPorHora(18)).toBe('Boa noite');
    expect(saudacaoPorHora(23)).toBe('Boa noite');
    expect(saudacaoPorHora(4)).toBe('Boa noite');
  });
});
