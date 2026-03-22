using UserPreferences.Models;
using Microsoft.EntityFrameworkCore;

namespace UserPreferences.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }

        public DbSet<UserPreference> UserPreferences { get; set; }
        public DbSet<HiddenAccount> HiddenAccounts { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<UserPreference>(entity =>
            {
                entity.HasIndex(e => new { e.UserId, e.AppType })
                    .IsUnique();

                entity.Property(e => e.Theme)
                    .HasMaxLength(10)
                    .IsRequired();
            });

            modelBuilder.Entity<HiddenAccount>(entity =>
            {
                entity.HasIndex(e => new { e.UserPreferenceId, e.AccountId })
                    .IsUnique();
            });
        }
    }
}
