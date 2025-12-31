import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function TeamsSite() {
  return (
    <DashboardLayout role="site">
      <div className="space-y-6">
        <h1 className="text-2xl font-bold">팀/노무자</h1>
        <Card>
          <CardHeader>
            <CardTitle>팀 및 노무자 관리</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center py-12 text-muted-foreground">
              팀 및 노무자 목록이 여기에 표시됩니다.
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}



