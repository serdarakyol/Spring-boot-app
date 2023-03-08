from locust import task, between, FastHttpUser
import string
import random
import time
import datetime

WAIT_TIME_MIN = 0.1
WAIT_TIME_MAX = 0.5

h = {
    "Content-Type": "application/json"
}

random.seed()

class LoadTest(FastHttpUser):
    wait_time = between(WAIT_TIME_MIN, WAIT_TIME_MAX)
    abstract = True
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.base_path = None
        self.saved_emails = []

    def generate_random_string(self, min_name_size=2, max_name_size=20) -> str:
        letters = string.ascii_lowercase
        string_size = random.randint(min_name_size, max_name_size)
        generated_string = ''.join(random.choice(letters) for i in range(string_size))
        return generated_string

    def generate_random_dob(self) -> str:
        d = random.randint(1, int(time.time()))
        return datetime.date.fromtimestamp(d).strftime('%Y-%m-%d')

    @task(1)
    def get_all(self):
        self.client.get(url=self.base_path)

    @task(1)
    def get_one_by_id(self):
        random_id = random.randint(1, len(self.saved_emails))
        self.client.get(url=f"{self.base_path}/by-id/{random_id}")

    @task(1)
    def get_one_by_email(self):
        random_email = random.choice(self.saved_emails)
        self.client.get(url=f"{self.base_path}/by-email/{random_email}")

    @task(20)
    def post_request(self):
        self.client.post(url=self.base_path, json=self._generate_post_data(), headers=h)

    @task(4)
    def put_request(self):
        request_path, request_data = self._generate_put_data()
        self.client.put(url=request_path, json=request_data, headers=h)


class TeacherProcess(LoadTest):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.base_path = "/api/v1/teacher"

    def on_start(self):
        self.client.post(url=self.base_path,
                         json=self._generate_post_data(),
                         headers=h)
    
    #def on_stop(self):
    #    time.sleep(1)
    #    for email in self.saved_emails:
    #        with self.client.delete(url=f"{self.base_path}/by-email/{email}",
    #                                catch_response=True) as response:
    #            # delete that after add logging to the API
    #            if response.status_code == 404:
    #                response.success()

    def _generate_post_data(self) -> dict:
        request_data = {
            "teacherName": str,
            "teacherEmail": str,
            "teacherDOB": str
        }
        request_data["teacherName"] = self.generate_random_string()
        request_data["teacherEmail"] = f"{self.generate_random_string()}@{self.generate_random_string()}.{self.generate_random_string()}"
        request_data["teacherDOB"] = self.generate_random_dob()
        self.saved_emails.append(request_data["teacherEmail"])

        return request_data

    def _generate_put_data(self) -> (str | object):
        teacher_email = f"{self.generate_random_string()}@{self.generate_random_string()}.{self.generate_random_string()}"
        request_data = {
            "teacherName": "Test name",
            "teacherEmail": teacher_email,
            "teacherDOB": "1995-03-07"
        }
        if len(self.saved_emails) > 0:
            request_data["teacherName"] = self.generate_random_string()
            request_data["teacherDOB"] = self.generate_random_dob()
            teacher_id = str(random.randint(1, len(self.saved_emails)))

            return f"{self.base_path}/{teacher_id}", request_data
        # this is added for handle None returns
        return f"{self.base_path}/1", request_data


class StudentProcess(LoadTest):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.base_path = "/api/v1/student"

    def on_start(self):
        self.client.post(url=self.base_path,
                         json=self._generate_post_data(),
                         headers=h)
    
    def on_stop(self):
        time.sleep(1)
        for email in self.saved_emails:
            with self.client.delete(url=f"{self.base_path}/by-email/{email}",
                                    catch_response=True) as response:
                # delete that after add logging to the API
                if response.status_code == 404:
                    response.success()

    def _generate_post_data(self) -> dict:
        request_data = {
            "studentName": str,
            "studentEmail": str,
            "studentDOB": str
        }
        request_data["studentName"] = self.generate_random_string()
        request_data["studentEmail"] = f"{self.generate_random_string()}@{self.generate_random_string()}.{self.generate_random_string()}"
        request_data["studentDOB"] = self.generate_random_dob()
        self.saved_emails.append(request_data["studentEmail"])

        return request_data
    
    def _generate_put_data(self) -> (str | object):
        student_email = f"{self.generate_random_string()}@{self.generate_random_string()}.{self.generate_random_string()}"
        request_data = {
            "studentName": "Test",
            "studentEmail": student_email,
            "studentDOB": "1999-03-07"
        }
        if len(self.saved_emails) > 0:
            request_data["studentName"] = self.generate_random_string()
            request_data["studentDOB"] = self.generate_random_dob()
            student_id = random.randint(1, len(self.saved_emails))
            self.saved_emails[student_id - 1] = student_email

            return f"{self.base_path}/{student_id}", request_data
        # this is added for handle None returns
        return f"{self.base_path}/1", request_data
