import { perfilTemAcesso, rotaInicialDoPerfil } from './perfil';

describe('perfil', () => {
  it('direciona cada perfil para a sua area', () => {
    expect(rotaInicialDoPerfil('GARCOM')).toBe('/garcom');
    expect(rotaInicialDoPerfil('COZINHA')).toBe('/cozinha');
    expect(rotaInicialDoPerfil('ADMIN')).toBe('/garcom');
  });

  it('admin tem acesso a qualquer area', () => {
    expect(perfilTemAcesso('ADMIN', ['GARCOM'])).toBe(true);
    expect(perfilTemAcesso('ADMIN', ['COZINHA'])).toBe(true);
  });

  it('perfil comum so acessa a propria area', () => {
    expect(perfilTemAcesso('GARCOM', ['GARCOM'])).toBe(true);
    expect(perfilTemAcesso('GARCOM', ['COZINHA'])).toBe(false);
  });

  it('sem perfil nao tem acesso', () => {
    expect(perfilTemAcesso(null, ['GARCOM'])).toBe(false);
  });
});
