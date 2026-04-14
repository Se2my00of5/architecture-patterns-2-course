namespace MonitoringService
{
    using Microsoft.EntityFrameworkCore;
    using MonitoringService.Models;
    using System.Collections.Generic;
    using System.Reflection.Emit;

    public class MonitoringDbContext : DbContext
    {
        public MonitoringDbContext(DbContextOptions<MonitoringDbContext> options) : base(options) { }

        public DbSet<TraceLog> TraceLogs { get; set; }
        public DbSet<ErrorLog> ErrorLogs { get; set; }
        public DbSet<Metric> Metrics { get; set; }

    }
}
