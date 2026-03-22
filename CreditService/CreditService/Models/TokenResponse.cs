namespace CreditService.Models
{
    class TokenResponse
    {
        public string AccessToken { get; set; } = "";
        public int ExpiresIn { get; set; }
    }
}
