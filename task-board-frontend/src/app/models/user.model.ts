export interface UserDetails {
  username: string | null;
  authProvider: string | null;
  providerId: string | null;
  displayName: string;
}

export interface UpdateUserCommand {
  displayName: string;
}
