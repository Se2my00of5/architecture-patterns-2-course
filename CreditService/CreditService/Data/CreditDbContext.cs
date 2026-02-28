using CreditService.Models;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Reflection.Emit;

namespace CreditService.Data
{
    public class CreditDbContext : DbContext
    {
        public CreditDbContext(DbContextOptions<CreditDbContext> options)
            : base(options)
        {
        }

        public DbSet<Credit> Credits { get; set; }
        public DbSet<CreditTariff> CreditTariffs { get; set; }
        public DbSet<CreditPayment> CreditPayments { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<Credit>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Amount).HasPrecision(18, 2);
                entity.Property(e => e.RemainingAmount).HasPrecision(18, 2);
                entity.Property(e => e.MonthlyPayment).HasPrecision(18, 2);

                entity.HasOne(e => e.Tariff)
                    .WithMany(t => t.Credits)
                    .HasForeignKey(e => e.TariffId)
                    .OnDelete(DeleteBehavior.Restrict);
            });

            modelBuilder.Entity<CreditTariff>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Name).IsRequired().HasMaxLength(100);
                entity.Property(e => e.InterestRate).HasPrecision(5, 2);
                entity.HasIndex(e => e.Name).IsUnique();
            });

            modelBuilder.Entity<CreditPayment>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Amount).HasPrecision(18, 2);

                entity.HasOne(e => e.Credit)
                    .WithMany(c => c.Payments)
                    .HasForeignKey(e => e.CreditId)
                    .OnDelete(DeleteBehavior.Cascade);
            });
        }
    }
}
