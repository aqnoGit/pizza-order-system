export type Perfil = 'CLIENTE' | 'GARCOM' | 'COZINHA' | 'ADMIN';

export const ROTA_LOGIN = '/login';

const ROTA_INICIAL_POR_PERFIL: Record<Perfil, string> = {
  GARCOM: '/garcom',
  COZINHA: '/cozinha',
  ADMIN: '/garcom',
  CLIENTE: ROTA_LOGIN,
};

export function rotaInicialDoPerfil(perfil: Perfil): string {
  return ROTA_INICIAL_POR_PERFIL[perfil];
}

export function perfilTemAcesso(perfil: Perfil | null, permitidos: readonly Perfil[]): boolean {
  return perfil !== null && (perfil === 'ADMIN' || permitidos.includes(perfil));
}
